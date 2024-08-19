package com.enterlib.data;

import com.enterlib.databinding.NotifyPropertyChanged;

import java.io.Serializable;

/**
 * Created by ansel on 25/09/2016.
 */
public class EntityObject<T> extends NotifyPropertyChanged implements Serializable {
    protected IEntityContext mapContext;
    private Class<T>cls;

    protected EntityObject(IEntityContext mapContext, Class<T> cls) {
        this.mapContext = mapContext;
        this.cls = cls;
    }

    protected EntityObject(IEntityContext mapContext) {
        this.mapContext = mapContext;
        this.cls = (Class<T>) getClass();
    }

    protected EntityObject(){
        this.cls = (Class<T>) getClass();
    }

    public boolean update(){
        if(mapContext == null)
            return  false;
        return  mapContext.update(cls, (T) this);
    }

    public boolean delete(){
        if(mapContext == null)
            return  false;
        return  mapContext.delete(cls, (T) this);
    }

    public boolean create(){
        if(mapContext == null)
            return false;
        return mapContext.create(cls, (T)this);
    }

    public <T> T get(Class<T>model, int id){
        if(id > 0 && mapContext!=null){
            return mapContext.get(model, id);
        }
        return null;
    }

}
