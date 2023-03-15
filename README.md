### build

Project requires only java to be installed (uses gradle wrapper) - to build with tests execute:

```shell
./gradlew build
```

### assumptions and decisions on performance

Assumption - multithreading must be supported, meaning all operation on OrderBook can be executed concurrently.

As there is high pressure on add/remove operations I've decided to use ConcurrentHashMap which have best performance for
multithreaded access. Also to not block those operations I've decided to move sorting which is time consuming to separate
threads. As there is no requirement on keeping model at all times&operations in sync which would require most likely
global sync I've decided to split model for sorting and handle it within separate threads - modify (main) and sorting threads
are communicating through blocking queue.
When querying for price/size/order there is assumption that it will be from given point in time and at the time it reaches client
it could potentially already change - I'm using copy of source data.

Alternate approach would be to have my own implementation of list that would always insert order in correct place - it would
take O(n) (where n is order count) on add/remove/modify, but it would have to execute on each operation - now it's more on
per request basis where multiple add/remove/modify could happen between taking snapshot and sorting whole list 
(quicksort complexity is O(n logn)) - which approach  would be better depends on ammount of data.

### Part B
Current solution is not durable or scalable (not counting sorting threads which is very limited scalability). For real life
scenario it would require to be connected to some scalable data source eg: kafka + data store like postgres, dynamodb depending
on the traffic.

Also if it would have to support operations like transaction meaning bid+offer execution there should be some consistency
mechanism in place like locking given order(s) or making sure it can be processed by only single thread at given time by
some partitioning+ordering rule (like in kafka).

If there would be a need to scale sorting functionality futher (eg so it could be handled by multiple threads) there could
be merge sort applied that allows this.
