
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

# Maven Repository
The library can be downloaded from [maven](https://dl.bintray.com/ansel86castro/enterlib). In addition to add the library to your project
add an additional repository to your gradle file as shown below:

```gradle

buildscript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/ansel86castro/enterlib" }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
```

After that you can add the depency as shown below:
```gradle
dependencies {
 ...... other dependencies

    compile 'com.cybtans:enterlib:2.0.0@aar'
 
 .......
}
```

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
```java
 EntityMapContext.deploy(this, "db.db3");
```
This operation will copy the file from the assets directory to the data directory of your application . The parameter "db.db3" is the filename of your sqlite database file in the assets folder. In addition the operation checks if the file was deployed to avoid deploying again. You can force the deploying calling `EntityMapContext.deploy(this, "db.db3", true);`

Next then create an `IEntityContext` as follows:

```java

IEntityContext context = new EntityMapContext(MainApp.this, "db.db3"); 
```
The `IEntityContext` works as a repository factory. You can use the `IRepository<T>` for persisting and query your entities.
```java

	IRepository<Transaction> map = context.getRepository(Transaction.class);
	ArrayList<Transaction> list = map.query().toList();
	
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
#### Performing CRUD Operations
```java
	transaction  = new Transaction();

	//Creates a new entity in the persisting store
	map.create(transaction);  
	
	//update the entity in the persisting store with new values  
	map.update(transaction);  
  
   //delete the entity from the persisting store
    map.delete(transaction);  
	
	//delete all entities that meet the condition
	map.delete("Description = 'Abc'");  
   
   //returns the total of entities in the store
	map.query().count();
	
	//return the first element in the query
	transaction = map.query().first();
```
####  Lazy Evaluation
Cursors are a way to iterate through the query in a efficient way due to the entities are loaded on demand so this optimize memory usage and it is the recommended mechanism for iterating large collections.
```java
IRepository<Transaction> map = context.getRepository(Transaction.class); 
IEntityCursor<Transaction> cursor = map.query().toCursor();
for (Transaction t: cursor ) {  
      //do something with t
}
cursor.close();
```

Also another more closed form can be use that is equivalent to the code above
```java
IRepository<Transaction> map = context.getRepository(Transaction.class); 
for (Transaction t: map.query() ) {  
      //do something with t
}
 
```

The `IEntityCursor<T>` provides the following interface
```java
public interface IEntityCursor<T> extends IClosable, Iterable<T> {  
		//return the total of elements in the query
	   int getCount();  
	  
	  //return an element if at the specified position
	  T getItem(int position);  
  }
```

#### Using filters and functions
Enterlib's ORM supports the following  functions

- sum(expression)
- avg(expression)
- count(expression)
- max(expression)
- min(expression)
- concat(expression): for string fields, returns the concatenations of the values 
- ifnull(exp1, exp2) :returns exp2 if exp1 is null
- contains([InverseNavigarionProperty].[expression]) : returns true if any element in the inverse navigation field meet the condition in [expression]

```java

  IRepository<Account> map = context.getRepository(Account.class);
  IQuerable<Account> querable  = map.query()
                .include("Currency")
                .where("CurrencyId.Name = 'USD'")
                .where("AVG(Transactions.Amount) > 5")
                .orderBy("Name desc")
                .skip(5)
                .take(10);
                
ArrayList<Transaction> list = querable.toList();
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

Another more complex example using and include chain

```java
    IRepository<Transaction> map = context.getRepository(Transaction.class);
    
    IQuerable<Transaction> query  = map.query()
            .include("Account.Currency")
            .where("Account.Currency.Name = 'USD'");

    System.out.println(query.toString());
```
Will produce the following sql

```sql
SELECT t0.Id as "Id"
,t0.Description as "Description"
,t0.Amount as "Amount"
,t0.AccountId as "AccountId"
,t1.Name as "AccountName"
,t2.Code as "CurrencyName"
,t1.Id as ".Account.Id"
,t1.Name as ".Account.Name"
,t1.CurrencyId as ".Account.CurrencyId"
,t2.Code as ".Account.CurrencyName"
,t2.CreateDate as ".Account.CurrencyCreateDate"
,total(t3.Amount) as ".Account.Balance"
,t2.Id as ".Account.Currency.Id"
,t2.Code as ".Account.Currency.Name"
,t2.CreateDate as ".Account.Currency.CreateDate"
FROM "Transactions" t0
INNER JOIN "Accounts" t1 on t1.Id = t0.AccountId 
INNER JOIN "Currencies" t2 on t2.Id = t1.CurrencyId 
LEFT OUTER JOIN "Transactions" t3 on t3.AccountId = t1.Id 
WHERE t2.Code = 'USD'
GROUP BY t2.Code,t0.Description,t1.Name,t2.Id,t0.Amount,t0.Id,t1.CurrencyId,t1.Id,t0.AccountId,t2.CreateDate
```
As you can see you can use this expression `Account.Currency.Name` or this `AccountId.CurrencyId.Name` in the `where` method because by convention if the field is not found the then ORM will look for a field with the given name but ending in "Id". 

#### Using nonMapped Columns for filtering
Suppose you want to retrieve all the accounts having its transactions's descriptions begining with 'Abc'.
```java
 IRepository<Account> map = context.getRepository(Account.class);
 IQuerable<Account> querable  = map.query()
               .where("Description like 'Abc%'");
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
WHERE t2.Description LIKE 'Abc%'
GROUP BY t0.Id,t1.CreateDate,t0.Name,t0.CurrencyId,t1.Code
```
For this to works you need to define the field **Description** in the Account classs as follows:
```java
@ColumnMap(column = "Id", nonMapped = true)  
@ForeingKey(model = Transaction.class, field = "AccountId")  
@ExpressionColumn(expr = "Transactions.Description")  
public String Description;
```
Using the annotations you can inform the ORM where to look for this Description column.  For example the `ExpressionColumn` sais the field is mapped to the `Description` member of the inverse navigation variable named `Transactions`.  But where this `Transactions` come from? 
This is specified by the `ForeingKey` annotation ,it  sais the  inverse navigation variable named `Transactions`is defined by the `Transaction` class and is related to the Account entity throught the `Transaction.AccountId` field with the `Account.Id` speficied in the `ColumnMap` annotation. The parameter `nonMapped = true` in the `ColumnMap` is to not include this Description column in the query result so it's use only for filtering purpose.

#### Using column alias name
You can query your entities for alias field like `CurrencyName` is an alias of `CurrencyId.Name`
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

#### Using  advance filter function
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
In the example above the we are getting all the Categories entities that are associated with a Account with Id = 5.
Again the `Category` class must specified the inverse navigation property `Accounts` to the relationship as shown bellow:
```java
@ColumnMap(column = "Id", nonMapped = true)  
@ForeingKey(model = AccountCategory.class, field = "CategoryId")  
public ArrayList<AccountCategory> Accounts;
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


