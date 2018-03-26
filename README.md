# Enterlib Android

Enterlib helps to decouple application components in to separated logical layers where the communications between them is through well-defined interfaces or code contracts. The architecture will help to write reusable, robust and testable components that can scale with with little changes in the code. Also the framework provides utilities for:

- Performing data binding
- Invoking Asynchronous operations
- Performing data validations and conversion
- Serializing components into JSON
- Invoking of RESTful HTTP Services
- Performing data filtering
- Communicating between loose couple components
- implementing the Repository pattern with common Interfaces
- Implementing Views
- Implementing ViewModels
- Additional set of Widgets

### Dependency Injection

Enterlib supports a depency injection engine using a `DependencyContext` object 

```java

class MainApp  extends Application {
 	
    DependencyContext dependencyContext;

	@Override
 	public void onCreate() {
        super.onCreate();

        dependencyContext = new DependencyContext();
        DependencyContext.setContext(dependencyContext);

        // register a factory give you flexibility for creating your instances. Required 
        //dependencies can be requested using the IServiceProvider object       
        dependencyContext.registerFactory(IEntityContext.class, new IDependencyFactory(){
                    @Override
                    public Object createInstance(IServiceProvider serviceProvider, Class<?> requestType) {
                        EntityMapContext dbContext = new EntityMapContext(MainApp.this, "db.db3");
                        return dbContext;
                    }
                }, LifeType.Default);

         dependencyContext.registerType(SomeService.class, LifeType.Scope);
         dependencyContext.registerSingleton(Context.class, this);
         dependencyContext.registerTypes(IDomainService.class, MyDomainService.class, LifeType.Scope);
         
         // Creating a scope 
         IDependencyContext scope = dependencyContext.createScope();
         
         //Objects resolved from the scope that has LifeType.Scope when registered in the scope or in any of its parent's scopes are cached so the same instance is reused per scope request.                        
         IDomainService service = scope.getService(IDomainService.class);
         
         //Registering types for using in a scope only
         scope.registerTypes(IScopedDomainService.class, MyScopedDomainService.class, LifeType.Scope);
         
         //disposing a scope frees from the scope cache all objects with LifeType.Scope 
         scope.dispose();
  	}
    
}
        
```
The `LifeType` enum specify how the depency infection engine creates the objects. It has the following options

- Default : The object is created every time.
- Scope: The object is created only onnce time per scope request.
- Singleton: The same instance is returned always.

### Data Access with Object Relational Mapping (ORM)

Enterlib provides a powerfull Object Relational Mapper (ORM) to Sqlite Databases. You just neeed to define your data models as simple Java classes with mapping annotations as follows:

```java
  @TableMap(name = "Accounts")
  public class Account{

        @ColumnMap(key = true)
        public int Id;

        @ColumnMap
        public String Name;

        @ColumnMap
        @ForeingKey(model = Currency.class)
        public int CurrencyId;
		
        //Including related object properties
        @ExpressionColumn(expr = "CurrencyId.Name")
        public String CurrencyName;
		
        //Including related object properties
        @ExpressionColumn(expr = "CurrencyId.CreateDate")
        public Date CurrencyCreateDate;

		//Aggregation Column
        @ColumnMap(column = "Id")
        @ForeingKey(model = Transaction.class, field = "AccountId")
        @ExpressionColumn(expr = "SUM(Transaction.Amount)")
        public double Balance;

		//Allow filtering for many-to-one relationships
        @ColumnMap(column = "Id", nonMapped = true)
        @ForeingKey(model = Transaction.class, field = "AccountId")
        @ExpressionColumn(expr = "Transaction.Description")
        public String Description;

		//Navigation Property
        
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

        @ColumnMap(key = true)
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

    }

    @TableMap(name = "Currencies")
    public class Currency{

        @ColumnMap(key = true)
        public int Id;

        @ColumnMap(column = "Code")
        public String Name;

        @ColumnMap
        public Date CreateDate;
    }

    public class Category{

        @ColumnMap(key = true)
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
```

First of all if you have stored you sqlite file in the assets directory you can deploy it calling 
```
 EntityMapContext.deploy(this, "db.db3");
```
This operation will copy the file from the assets directory to the data directory of your application . The parameter "db.db3" is the filename of your sqlite database file in the assets folder. In addition the operation checks if the file was deployed to avoid deploying again. You can force the deploying calling `EntityMapContext.deploy(this, "db.db3", true);`

Next then create an `EntityContext` as follows:

```java

IEntityContext context = new EntityMapContext(MainApp.this, "db.db3"); 
```

```java
IRepository<Transaction> map = entityContext.getRepository(Transaction.class);

ArrayList<Transaction> list = map.query().toList();
transaction = map.query().first();

```
Generates the following SQL

```sql
SELECT t0.Description as "Description"
,t0.AccountId as "AccountId"
,t0.Amount as "Amount"
,t0.Id as "Id"
,t1.Name as "AccountName"
,t2.Code as "CurrencyName"
FROM "Transactions" t0
INNER JOIN "Accounts" t1 on t1.Id = t0.AccountId 
INNER JOIN "Currencies" t2 on t2.Id = t1.CurrencyId 

```
using filters and aggregations
```java

  IRepository<Account> map = context.getRepository(Account.class);
  IQuerable<Account> querable  = map.query()
                .include("Currency")
                .where("CurrencyId.Name = 'USD'")
                .where("AVG(Transaction.Amount) > 5")
                .orderBy("Name desc")
                .skip(5)
                .take(10);
                
ArrayList<Transaction> list = querable.toList();

ArrayList<Transaction>
```

Will generate the following SQL
```sql
SELECT t1.CreateDate as "CurrencyCreateDate"
,t0.Id as "Id"
,total(t2.Amount) as "Balance"
,t0.CurrencyId as "CurrencyId"
,t0.Name as "Name"
,t1.Code as "CurrencyName"
,t1.Id as ".Currency.Id"
,t1.CreateDate as ".Currency.CreateDate"
,t1.Code as ".Currency.Name"
FROM "Accounts" t0
INNER JOIN "Currencies" t1 on t1.Id = t0.CurrencyId 
LEFT OUTER JOIN "Transactions" t2 on t2.AccountId = t0.Id 
WHERE t1.Code = 'USD'
GROUP BY t0.Id,t1.CreateDate,t0.Name,t1.Id,t0.CurrencyId,t1.Code
HAVING avg(t2.Amount) > 5
ORDER BY t0.Name DESC
LIMIT 10
OFFSET 5
```

Using nonMapped Columns for filtering

```java
 IRepository<Account> map = context.getRepository(Account.class);
 IQuerable<Account> querable  = map.query()
                .where("Description = 'Abc'")
                .skip(5)
                .take(10);
```
Generates the following SQL

```sql
SELECT t1.CreateDate as "CurrencyCreateDate"
,t0.Id as "Id"
,total(t2.Amount) as "Balance"
,t0.CurrencyId as "CurrencyId"
,t0.Name as "Name"
,t1.Code as "CurrencyName"
FROM "Accounts" t0
INNER JOIN "Currencies" t1 on t1.Id = t0.CurrencyId 
LEFT OUTER JOIN "Transactions" t2 on t2.AccountId = t0.Id 
WHERE t0.Id = 'Abc'
GROUP BY t0.Id,t1.CreateDate,t0.Name,t0.CurrencyId,t1.Code
LIMIT 10
OFFSET 5
```

using column alias
```java
 IRepository<Account> map = context.getRepository(Account.class);
 IQuerable<Account> querable  = map.query()
                .where("CurrencyName = 'EUR'");
```
Generates the following SQL
```sql
SELECT t1.CreateDate as "CurrencyCreateDate"
,t0.Id as "Id"
,total(t2.Amount) as "Balance"
,t0.CurrencyId as "CurrencyId"
,t0.Name as "Name"
,t1.Code as "CurrencyName"
FROM "Accounts" t0
INNER JOIN "Currencies" t1 on t1.Id = t0.CurrencyId 
LEFT OUTER JOIN "Transactions" t2 on t2.AccountId = t0.Id 
WHERE t1.Code = 'EUR'
GROUP BY t0.Id,t1.CreateDate,t0.Name,t0.CurrencyId,t1.Code

```

Using `contains` filter function

```java
IRepository<Category> map = context.getRepository(Category.class);
IQuerable<Category> querable  = map.query()
              .where("CONTAINS(Accounts.AccountId = 5)");
```

Generates the following SQL
```sql
SELECT t0.Id as "Id"
,t0.Name as "Name"
FROM "Category" t0
WHERE t0.Id IN (SELECT t0.CategoryId as "CategoryId"
FROM "AccountCategory" t0
WHERE t0.AccountId = 5)
```
       
A more complex example using `contains` 
```java
 IRepository<Category> map = context.getRepository(Category.class);
 IQuerable<Category> querable  = map.query()
                .where("CONTAINS( Accounts.CategoryId = 5 && COUNT(Accounts.AccountId) > 5)");
```

Generates the following SQL
```sql
SELECT t0.Id as "Id"
,t0.Name as "Name"
FROM "Category" t0
WHERE t0.Id IN (SELECT t0.CategoryId as "CategoryId"
FROM "AccountCategory" t0
GROUP BY t0.CategoryId
HAVING (t0.CategoryId = 5) AND (count(t0.AccountId) > 5))
```

using the `exclude` filter function
```java
IRepository<Category> map = context.getRepository(Category.class);
IQuerable<Category> querable  = map.query()
                .where("EXCLUDE(Accounts.AccountId.CurrencyId.Name = 'UYU')");

```
Generates the following SQL
```sql
SELECT t0.Id as "Id"
,t0.Name as "Name"
FROM "Category" t0
WHERE t0.Id NOT IN (SELECT t0.CategoryId as "CategoryId"
FROM "AccountCategory" t0
INNER JOIN "Accounts" t1 on t1.Id = t0.AccountId 
INNER JOIN "Currencies" t2 on t2.Id = t1.CurrencyId 
WHERE t2.Code = 'UYU')
```



      
