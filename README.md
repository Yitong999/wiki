**CPEN 221 / Fall 2021 / MP3**

# Buffers, Concurrency and Wikipedia

This mini-project involves interacting with Wikipedia and performing many operations.

Some of the learning goals for this mini-project are:

* Working with external libraries (such as `jwiki` and `gson`);
* Implementing reusable datatypes such as a `FSFTBuffer`;
* Using multi-threading to handle certain aspects of the implementation;
* Managing shared memory when multiple threads are involved;
* Implementing parsers for a given grammar and executing queries on a database.

In this assignment, you will:

- Use external libraries and APIs for data processing;
- Implement concurrent processing of related operations;
- Implement core computing abstractions such as caching;
- Parse and execute structured queries.

You will continue to work with [Java Generics](https://docs.oracle.com/javase/tutorial/java/generics/why.html) to produce a reusable buffer abstraction.

Read this `README` in its entirety. There are five tasks involved. The assignment may appear more intimidating than it actually is. Completing one task at a time may be a good tactic.

## Task 1: Implement `FSFTBuffer`

The first task is to implement a parametrized datatype that stores a finite number of objects of type **T** for a finite amount of time: we will call such a buffer a **finite-space finite-time buffer**. Only a finite amount of objects can be added to such a buffer. Furthermore, an object added to the buffer are retained only for a finite amount of time unless the object is accessed, updated or touched.

When an object is to be added to an instance of FSFTBuffer and the buffer is at capacity, the least recently used object is removed to make space for the new object.

An `FSFTBuffer` supports the following operations:

* `FSFTBuffer(int capacity, int timeout)`: Create an instance of `FSFTBuffer` with a given `capacity` and with `timeout` representing the duration of time (in seconds) for which an object should be retained in the buffer (unless it is removed because of capacity limitations).

* `boolean put(T t)`: add a value to the buffer and return true if the value was successfully added and false otherwise. When a value is added to an instance of `FSFTBuffer` and the buffer is full then the new object should remove the least recently used object. (Note that objects that have timed out should be remove first.)
* `T get(String id)`: Retrieve an object from the buffer. When an object is retrieved at time **timeInSeconds** from the buffer, it is "used" at that time and it will now timeout at the absolute time **timeInSeconds + timeout**.
* `boolean touch(String id)`: This method, when called at time **timeInSeconds**, updates the absolute timeout time for the object with `id` to **timeInSeconds + timeout**. This method returns **true** if an object was touched and **false** if no object with `id` exists in the buffer.

An **FSFTBuffer** can be used to implement a data cache.

## Task 2: Make `FSFTBuffer` **Thread-Safe**

In this task, you should ensure that your implementation of `FSFTBuffer` can handle multiple threads writing to and reading from the same instance of `FSFTBuffer`. This means that many `put` and `get` operations should proceed in concurrently.

## Task 3: `WikiMediator`

For this task, you should implement a mediator service for Wikipedia. This service will access Wikipedia (using the `JWiki` API) to obtain pages and other relevant information. 

* The mediator service should **cache** Wikipedia pages to minimize network accesses. 
The cache capacity (number of pages to be cached) as well as the staleness interval (the number of **seconds** after which a page in the cache will become stale) will be provided as arguments to the constructor for this class:
```java
public WikiMediator(int capacity, int stalenessInterval)
```
* The mediator service should also collect statistical information about requests.

A `WikiMediator` instance should support the following basic operations:

1. `List<String> search(String query, int limit)`: Given a `query`, return up to `limit` page titles that match the query string (per Wikipedia's search service).
2. `String getPage(String pageTitle)`: Given a `pageTitle`, return the text associated with the Wikipedia page that matches `pageTitle`.
3. `List<String> zeitgeist(int limit)`: Return the most common `String`s used in `search` and `getPage` requests, with items being sorted in non-increasing count order. When many requests have been made, return only `limit` items.
4. `List<String> trending(int timeLimitInSeconds, int maxItems)`: Similar to `zeitgeist()`, but returns the most frequent requests made in the last `timeLimitInSeconds` seconds. This method should report at most `maxItems` of the most frequent requests. 
5. `int windowedPeakLoad(int timeWindowInSeconds)`: What is the maximum number of requests seen in any time window of a given length? The request count is to include all requests made using the public API of `WikiMediator`, and therefore counts all **five** methods listed as **basic page requests**. (There is one more request that appears later, `shortestPath`, and that should also be included if you do implement that method.)
6. `int windowedPeakLoad()`: This is an overloaded version of the previous method where the time window defaults to 30 seconds. (Calls to this method also affect peak load.)

## Task 4: `WikiMediatorServer`

### **Network Service**

Implement a server application that wraps a `WikiMediator` instance. The server should receive requests over a network. Implement a server-based application that receives requests over a network socket and returns results appropriately. The server should be capable of handling more than one request simultaneously.

(To get started, you will find this example helpful: https://github.com/CPEN-221/FibonacciServer.)

The requests take the form of a JSON-formatted string with appropriate parameters. Each request has a `type` that indicates the operation that needs to be performed and other fields in the JSON-formatted string use the same name as the parameters for the operations.

As examples, here are strings for `search` and `zeitgeist`:

```json
{
	"id": "1",
	"type": "search",
	"query": "Barack Obama",
	"limit": "12"
}

{
	"id": "two",
	"type": "zeitgeist",
	"limit": "5"
}
```

The `id` field is an identifier used by the client to disambiguate multiple responses and should be included as-is in the response.

The response should also be a JSON-formatted string with a `status` field that should have the value `"success"` if the operation was successfully executed, and a `response` field that contains the results. 
If the operation was not successful then the `status` field should have the value `"failed"` and the `response` field can include a suitable error message explaining the failure.

For example, the response to the simple search with "Barack Obama" should yield:

```json
{
	"id": "1",
	"status": "success",
	"response": ["Barack Obama", "Barack Obama in comics", "Barack Obama Sr.", "List of things named after Barack Obama", "Speeches of Barack Obama"]
}
```

The JSON-formatted request may include an optional `timeout` field that indicates how long (in seconds) the service should wait for a response from Wikipedia before declaring the operation as having failed. 
For example, the following request

```json
{
	"id": "3",
	"type": "search",
	"pageTitle": "Philosophy",
	"timeout": "1"
}
```

may fail because no Wikipedia response was received in 1 second resulting in a `WikiMediator` response such as this:

```json
{
	"id": "3",
	"status": "failed",
	"response": "Operation timed out"
}
```

### Survivability Across Sessions

You should implement a system where the statistical information associated with the `WikiMediator` instance can be stored in the local filesystem, and that such data can be reloaded each time your service is started. 
You **should** use the directory `local` for all the files that you create.

To shutdown a server, one would send a request like this:

```json
{
	"id": "ten",
	"type": "stop"
}
```

The server should respond with the message:

```json
{
        "id": "ten", 
        "response": "bye"
}
```

And then the server should stop accepting requests over the network and terminate after writing state to disk. This state should be read when a new instance of `WikiMediatorServer` is created and the data is available in the directory named `local`.

## Task 5: Shortest Path Between Two Pages

The last part of this mini-project is to add support to `WikiMediator` to find the shortest path between two Wikipedia pages.  
Basically, we are interested to know the minimum number of link clicks it takes to start from a page and reach another page.
To do so, a method with the following signature should be included in `WikiMediator`:
```java
List<String> shortestPath(String pageTitle1, String pageTitle2, int timeout) throws TimeoutException
```
* If a path exists, a list of page titles (including the starting and ending pages) on the shortest path should be returned.
* If there are two or more shortest paths then the one with the lowest lexicographical value is to be returned.
* If no path exists between two pages, an empty `List` should be returned. 

The `timeout` parameter is the duration - in seconds - that is permitted for this operation, otherwise a [`TimeoutException`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/TimeoutException.html) should be thrown.

`WikiMediatorServer` should also support this operation using the following `JSON` request:

```json
{
	"id": "3",
	"type": "shortestPath",
	"pageTitle1": "Philosophy",
	"pageTitle2": "Barack Obama",
	"timeout": "30"
}
```

and such a request *could* have the following response (this example may not be the lexicographically smallest response):

```json
  "id": "3",
  "status": "success",
  "response": ["Philosophy", "African Americans", "Barack Obama"]
```

that indicates that there is a two-step path from "Philosophy" to "Barack Obama" via "African Americans". 

If there is no path then `"status"` should be `"success"` and `"response"` should be the empty array `[]`. In the case of a timeout, `"status"` should be `"failed"` and `"response"` would be `"Operation timed out"`.

## Assessment Hints

- You should write specs, rep invariants, abstraction functions, and thread-safety conditions.
- You should test your code and achieve: 
   - 95% lines of code coverage;
   - 85% branch coverage.
- Grading is holistic but here is an approximation:
  - Each task is worth one point.
  - An extra one point is for writing clean and modular code.
  - If tasks are functionally correct but the overall submission is lacking in essentials such as specs, RIs, AFs and thread-safety conditions or is lacking in test coverage then the grade will be lowered.
- Conform to the expected method signatures. If in doubt, ask!

## Hints

**JWiki**

Some starter code is provided in the package `example` to help you with the `JWiki` API. You will, however, have to make changes to the `build.gradle` file to include [this library](https://github.com/fastily/jwiki) as a dependency.

**Gson**

You will be using JSON for exchanging information with a server. [The `Gson` API](https://github.com/google/gson) will simplify the processing of JSON requests. You should add `Gson` as a dependency in `build.gradle`.

**`FSFTBuffer` and Multithreading**

You do not need to use multithreading to implement `FSFTBuffer`. On the other hand, a non-multithreaded implementation often involves combining multiple operations into one method, which is sometimes hard to maintain.

**Staleness vs. Least Recently Used**

The implementation of `FSFTBuffer` has two time-related aspects:

1. Is an item stale? An item is stale if it has been in the cache for a long time and the original version of the item may have been mutated. Such items should be removed from the cache when they go stale. Or, in any case, should not be considered as occupying space in the cache.
2. Evicting items from the cache: When a cache is full, you will have to remove items that have not been accessed recently. ("Recent access" has nothing to do with "staleness".) Why is least-recently-used replacement a good policy? It turns out the identifying the optimal policy for cache eviction is hard. LRU has some good properties and is known to be a competitive algorithm when compared with an oracle that knows the future and can decide what is the best item to evict. Note that stale items should not count towards cache occupancy and should be removed before evicting non-stale items.

**`Bufferable`**

The `Bufferable` interface has exactly one method. You should not add methods to this interface. Also, your implementation of `FSFTBuffer` should work with any datatype that implements the `Bufferable` interface so you cannot rely on additional information/methods being available from items you have to cache.

Note that `FSFTBuffer` need not implement `Bufferable`. (In principle, a buffer of buffers can be imagined but is not very useful.)

**Buffer Accesses**

Only `getPage()` requests affect the buffer/cache as far as the `WikiMediator` implementation is concerned.

**`touch` and `update`**

`FSFTBuffer` should support these methods but you are not expected to use these methods in `WikiMediator`. One can imagine background threads that keep items fresh in the cache using such methods but I am not expecting you to implement such features.

**Do `put` and `update` affect access times?**

The first `put` of an item into a cache does affect its access time. You can decide whether repeated `put`s of the same item and `update`s of an item are "accesses" of the item in the cache; I would not consider them to be so.

**`trending` and `zeitgeist`**

These methods only count the `String`s passed as arguments and not the actual method calls.

`zeitgeist` was something Google used to support and has evolved into [Google Trends](https://trends.google.com/trends/?geo=US).

**peakLoad30s**: This method tracks all requests to a `WikiMediator` instance including requests for `peakLoad30s`.

**Storing statistics**

- It is sufficient to log all requests in some form to disk so that when an instance of `WikiMediator` is started (after a shutdown) one can restore data needed to compute statistics for `trending`, `zeitgeist` and `peakLoad30s`. You may want to think carefully about when your write data to disk.
- We will use the directory name `local` with no further path specifications when we test your implementation. A directory with this name exists in the skeleton code structure that was provided and that is where you should storing all the data. If this directory is empty (except for the `.keep` file) then you should assume there is no history to load.

**Multiple requests**

You should be able to process multiple client requests simultaneously. The level of concurrency is dictated by the parameter `n` that is used to instantiate a `WikiMediatorServer`. A client can send more than one request before closing a session. Each client request will involve a single method invocation at the server. (You do not need to plan for clients bundling multiple requests into one message.)

**Expectations around client requests**

It is acceptable to assume that all requests from clients will be well-formed. It is not difficult to write handlers for ill-formed requests but you can assume, for now, that the requests will be correctly formatted.

**Handling timeouts**

You should not need to change `WikiMediator` to handle timeouts. You can use Java's `ExecutorService` and `Future`s if necessary. Note that you have to handle `n` concurrent clients but this does not mean that you are restricted to `n` threads. Using some additional threads may allow you to realize the abstraction required.