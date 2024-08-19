
package com.enterlib;

import android.util.Log;

import com.enterlib.annotations.ColumnMap;
import com.enterlib.annotations.ExpressionColumn;
import com.enterlib.annotations.ForeingKey;
import com.enterlib.annotations.TableMap;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.sqlite.EntityMap;
import com.enterlib.data.sqlite.EntityMapContext;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class SQLQueryTest {

    EntityMapContext context = new EntityMapContext(null);

    @TableMap(name = "Accounts")
    public class Account{

        @ColumnMap(key = true, writable = false)
        public int Id;

        @ColumnMap
        public String Name;

        @ColumnMap
        @ForeingKey(model = Currency.class)
        public int CurrencyId;

        @ExpressionColumn(expr = "CurrencyId.Name")
        public String CurrencyName;

        @ExpressionColumn(expr = "CurrencyId.CreateDate")
        public Date CurrencyCreateDate;

        @ColumnMap(column = "Id")
        @ForeingKey(model = Transaction.class, field = "AccountId")
        @ExpressionColumn(expr = "SUM(Transactions.Amount)")
        public double Balance;

        @ColumnMap(column = "Id", nonMapped = true)
        @ForeingKey(model = Transaction.class, field = "AccountId")
        @ExpressionColumn(expr = "Transactions.Description")
        public String Description;


        private Currency currency;
        public Currency getCurrency(){
            return currency;
        }
        public void setCurrency(Currency value){
            this.currency = value;
        }
    }

    @TableMap(name = "Transactions")
    public class Transaction{

        @ColumnMap(key = true, writable = false)
        public int Id;

        @ColumnMap
        public String Description;

        @ColumnMap
        public double Amount;

        @ColumnMap
        @ForeingKey(model = Account.class)
        public int AccountId;

        @ExpressionColumn(expr = "AccountId.Name")
        public String AccountName;

        @ExpressionColumn(expr = "AccountId.CurrencyId.Name")
        public String CurrencyName;

        private Account account;

        public Account getAccount(){
            return account;
        }

        public void setAccount(Account value){
            this.account = value;
        }

    }

    @TableMap(name = "Currencies")
    public class Currency{

        @ColumnMap(key = true, writable = false)
        public int Id;

        @ColumnMap(column = "Code")
        public String Name;

        @ColumnMap
        public Date CreateDate;
    }

    public class Category{

        @ColumnMap(key = true, writable = false)
        public int Id;

        @ColumnMap
        public String Name;

        @ColumnMap(column = "Id", nonMapped = true)
        @ForeingKey(model = AccountCategory.class, field = "CategoryId")
        public ArrayList<AccountCategory> Accounts;
    }

    public class AccountCategory{

        @ColumnMap(key = true, order = 0)
        @ForeingKey(model = Account.class, field = "Id")
        public int AccountId;

        @ColumnMap(key = true, order = 1)
        @ForeingKey(model = Category.class, field = "Id")
        public int CategoryId;
    }

    @Test
    public void ShouldGenerateDefaultSQLForTransactions(){
        System.out.println("ShouldGenerateDefaultSQLForTransactions");

        IRepository<Transaction> map = context.getMap(Transaction.class);
        IQuerable<Transaction> query  = map.query();
        String sql = query.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Description as \"Description\"\n" +
                ",t0.Amount as \"Amount\"\n" +
                ",t0.AccountId as \"AccountId\"\n" +
                ",t1.Name as \"AccountName\"\n" +
                ",t2.Code as \"CurrencyName\"\n" +
                "FROM \"Transactions\" t0\n" +
                "INNER JOIN \"Accounts\" t1 on t1.Id = t0.AccountId \n" +
                "INNER JOIN \"Currencies\" t2 on t2.Id = t1.CurrencyId ",sql);

    }

    @Test
    public void ShouldGenerateDefaultSQLForAccounts(){
        System.out.println("ShouldGenerateDefaultSQLForAccounts");

        EntityMap<Account> map = context.getMap(Account.class);
        IQuerable<Account> query  = map.query();
        String sql = query.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t0.CurrencyId,t1.Code", sql);
    }

    @Test
    public void ShouldGenerateSQLForTransacitionWithInclude(){
        System.out.println("ShouldGenerateQLForAccountsWithInclude");

        IRepository<Transaction> map = context.getRepository(Transaction.class);
        IQuerable<Transaction> query  = map.query()
                .include("Account.Currency")
                .where("Account.Currency.Name = 'USD'");
        String sql = query.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Description as \"Description\"\n" +
                ",t0.Amount as \"Amount\"\n" +
                ",t0.AccountId as \"AccountId\"\n" +
                ",t1.Name as \"AccountName\"\n" +
                ",t2.Code as \"CurrencyName\"\n" +
                ",t1.Id as \".Account.Id\"\n" +
                ",t1.Name as \".Account.Name\"\n" +
                ",t1.CurrencyId as \".Account.CurrencyId\"\n" +
                ",t2.Code as \".Account.CurrencyName\"\n" +
                ",t2.CreateDate as \".Account.CurrencyCreateDate\"\n" +
                ",total(t3.Amount) as \".Account.Balance\"\n" +
                ",t2.Id as \".Account.Currency.Id\"\n" +
                ",t2.Code as \".Account.Currency.Name\"\n" +
                ",t2.CreateDate as \".Account.Currency.CreateDate\"\n" +
                "FROM \"Transactions\" t0\n" +
                "INNER JOIN \"Accounts\" t1 on t1.Id = t0.AccountId \n" +
                "INNER JOIN \"Currencies\" t2 on t2.Id = t1.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t3 on t3.AccountId = t1.Id \n" +
                "WHERE t2.Code = 'USD'\n" +
                "GROUP BY t2.Code,t0.Description,t1.Name,t2.Id,t0.Amount,t0.Id,t1.CurrencyId,t1.Id,t0.AccountId,t2.CreateDate", sql);
    }



    @Test
    public void ShouldGenerateSQLForAccountsWithFilters(){
        System.out.println("ShouldGenerateSQLForAccountsWithFilters");

        EntityMap<Account> map = context.getMap(Account.class);
         IQuerable<Account> querable  = map.query()
                .include("Currency")
                .where("CurrencyId.Name = 'USD'")
                .where("AVG(Transactions.Amount) > 5")
                .orderBy("Name desc")
                .skip(5)
                .take(10);

        String sql = querable.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                ",t1.Id as \".Currency.Id\"\n" +
                ",t1.Code as \".Currency.Name\"\n" +
                ",t1.CreateDate as \".Currency.CreateDate\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "WHERE t1.Code = 'USD'\n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t1.Id,t0.CurrencyId,t1.Code\n" +
                "HAVING avg(t2.Amount) > 5\n" +
                "ORDER BY t0.Name DESC\n" +
                "LIMIT 10\n" +
                "OFFSET 5", sql);
    }

    @Test
    public void ShouldGenerateSQLForAccountsWithAlias1(){
        System.out.println("ShouldGenerateSQLForAccountsWithAlias1");

        EntityMap<Account> map = context.getMap(Account.class);
        IQuerable<Account> querable  = map.query()
                .where("Description like 'Abc%'");

        String sql = querable.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "WHERE t2.Description LIKE 'Abc%'\n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t0.CurrencyId,t1.Code", sql);
    }

    @Test
    public void ShouldGenerateSQLForAccountsWithAlias2(){
        System.out.println("ShouldGenerateSQLForAccountsWithAlias2");

        EntityMap<Account> map = context.getMap(Account.class);
        IQuerable<Account> querable  = map.query()
                .where("CurrencyName = 'EUR'");
        String sql = querable.toString();

        System.out.println(sql);


        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "WHERE t1.Code = 'EUR'\n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t0.CurrencyId,t1.Code",sql);
    }

    @Test
    public void ShouldGenerateSQLForAccountsWithHaving(){
        System.out.println("ShouldGenerateSQLForAccountsWithHaving");

        EntityMap<Account> map = context.getMap(Account.class);
        IQuerable<Account> querable  = map.query()
                .where("CurrencyId.Name = 'USD'")
                .where("AVG(Transactions.Amount) > 5")
                .where("Description = 'Abc'")
                .orderBy("Name desc")
                .skip(5)
                .take(10);

        String sql = querable.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "WHERE t1.Code = 'USD' AND t2.Description = 'Abc'\n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t0.CurrencyId,t1.Code\n" +
                "HAVING avg(t2.Amount) > 5\n" +
                "ORDER BY t0.Name DESC\n" +
                "LIMIT 10\n" +
                "OFFSET 5",sql);

    }

    @Test
    public void ShouldGenerateSQLForAccountsWithNoTransactions(){
        System.out.println("ShouldGenerateSQLForAccountsWithNoTransactions");

        EntityMap<Account> map = context.getMap(Account.class);
        IQuerable<Account> querable  = map.query()
                .include("Currency")
                .where("CurrencyId.Name = 'USD'")
                .where("COUNT(Transactions.Id) = 0")
                .orderBy("Name desc")
                .skip(5)
                .take(10);

        String sql = querable.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                ",t0.CurrencyId as \"CurrencyId\"\n" +
                ",t1.Code as \"CurrencyName\"\n" +
                ",t1.CreateDate as \"CurrencyCreateDate\"\n" +
                ",total(t2.Amount) as \"Balance\"\n" +
                ",t1.Id as \".Currency.Id\"\n" +
                ",t1.Code as \".Currency.Name\"\n" +
                ",t1.CreateDate as \".Currency.CreateDate\"\n" +
                "FROM \"Accounts\" t0\n" +
                "INNER JOIN \"Currencies\" t1 on t1.Id = t0.CurrencyId \n" +
                "LEFT OUTER JOIN \"Transactions\" t2 on t2.AccountId = t0.Id \n" +
                "WHERE t1.Code = 'USD'\n" +
                "GROUP BY t0.Id,t0.Name,t1.CreateDate,t1.Id,t0.CurrencyId,t1.Code\n" +
                "HAVING count(t2.Id) = 0\n" +
                "ORDER BY t0.Name DESC\n" +
                "LIMIT 10\n" +
                "OFFSET 5",sql);
    }

    @Test
    public void ShouldGenerateSQLForSubQuery(){
        System.out.println("ShouldGenerateSQLForSubQuery");

        EntityMap<Category> map = context.getMap(Category.class);
        IQuerable<Category> querable  = map.query()
              .where("CONTAINS(Accounts.AccountId = 5)");

        String sql = querable.toString();
        System.out.println(sql);

       Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                "FROM \"Category\" t0\n" +
                "WHERE t0.Id IN (SELECT t0.CategoryId as \"CategoryId\"\n" +
                "FROM \"AccountCategory\" t0\n" +
                "WHERE t0.AccountId = 5)",sql);
    }

    @Test
    public void ShouldGenerateSQLForSubQueryWithAggregateCondition(){
        System.out.println("ShouldGenerateSQLForSubQueryWithAggregateCondition");

        EntityMap<Category> map = context.getMap(Category.class);
        IQuerable<Category> querable  = map.query()
                .where("CONTAINS(COUNT(Accounts.AccountId) > 5)");

        String sql = querable.toString();
        System.out.println(sql);

        Assert.assertEquals("SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                "FROM \"Category\" t0\n" +
                "WHERE t0.Id IN (SELECT t0.CategoryId as \"CategoryId\"\n" +
                "FROM \"AccountCategory\" t0\n" +
                "GROUP BY t0.CategoryId\n" +
                "HAVING count(t0.AccountId) > 5)", sql);
    }

    @Test
    public void ShouldGenerateSQLForExcludeSubQuery(){
        System.out.println("ShouldGenerateSQLForExcludeSubQuery");

        EntityMap<Category> map = context.getMap(Category.class);
        IQuerable<Category> querable  = map.query()
                .where("EXCLUDE(Accounts.AccountId.CurrencyId.Name = 'UYU')");

        String sql = querable.toString();
        System.out.println(sql);

        String expected ="SELECT t0.Id as \"Id\"\n" +
                ",t0.Name as \"Name\"\n" +
                "FROM \"Category\" t0\n" +
                "WHERE t0.Id NOT IN (SELECT t0.CategoryId as \"CategoryId\"\n" +
                "FROM \"AccountCategory\" t0\n" +
                "INNER JOIN \"Accounts\" t1 on t1.Id = t0.AccountId \n" +
                "INNER JOIN \"Currencies\" t2 on t2.Id = t1.CurrencyId \n" +
                "WHERE t2.Code = 'UYU')";

        Assert.assertEquals(sql, expected);
    }

}

