package com.enterlib.data.web;

import android.text.TextUtils;

import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.InvalidOperationException;

/**
 * Created by hp on 10/28/2016.
 */
public class ManyToManyApiClient<TRelation, TModel> extends WebApiClient<TModel>  {

    private final int id;
    private final Class<TRelation> relation;
    private final boolean distint;
    //model forering key
    private PropertyMap fkey;
    //to one foreign key
    private PropertyMap key;

    public ManyToManyApiClient(WebApiContext context, String baseUrl, Class<TRelation> relation, Class<TModel> model, int id ,boolean distint){
        super(context, model, baseUrl, relation.getSimpleName()+"__"+model.getSimpleName());
        this.id = id;
        this.relation = relation;
        this.distint = distint;
        init();
    }

    public void init(){
        WebApiClient<TRelation> relMap = webContext.getClient(relation);
        WebApiClient<TModel> modelMap = webContext.getClient(getCls());

        String modelName= modelMap.getController();

        PropertyMap[] keys = relMap.getKeys();
        fkey = null;
        for (int i = 0; i < keys.length; i++) {
            PropertyMap key = keys[i];
            if(key.IsForeignKey){
                String fkeyTable = key.FKey_table();
                if(fkeyTable!=null && fkeyTable.equals(modelName)) {
                    if (key.FKey_To() == null)
                        throw new InvalidOperationException("Missing property 'to' in @ForeingKey annotation for " + relation.getSimpleName());
                    fkey = key;
                    break;
                }
            }
        }

        if(fkey == null)
            throw new InvalidOperationException("Unable to determine @ForeingKey in "+relation.getSimpleName());

        for (int i = 0; i < keys.length; i++) {
            if(fkey!=keys[i]){
                key = keys[i];
                break;
            }
        }

        relMap.close();
        modelMap.close();
    }

    @Override
    protected String getRequestUrl(String segments, String query) {
        if(TextUtils.isEmpty(query)){
            query = "targetId="+String.valueOf(id);
        }else{
            query += "&targetId="+String.valueOf(id);
        }

        query+="&distint="+String.valueOf(distint);

        return super.getRequestUrl(segments, query);
    }


}
