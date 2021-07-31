# Signals

A loosely coupled observer system implementation inspired by boost::signals/glibmm signals.

## Why?

Observers are an important part of the arsenal for any software designer and provide extraction and loose coupling of
side-effects from operations.

Typical observers need a lot of boilerplate code to register and handle observer calls. Missing safety measures can
break host logic. Also, depending on use case, we might want to invoke observers in sync/async, fire-forget modes etc.

Signals provides a clean abstraction to implement observers with low overhead.

## Getting Started

There are 3 basic steps involved in the process:

1. Create a signal
2. Connect signal handlers to signal
3. Dispatch the signal when you want to trigger observers

### Sample Code

Add the following dependency to `pom.xml`:

```xml

<dependency>
    <groupId>io.appform.signals</groupId>
    <artifactId>signals</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

The following seciton is some random java code to show how it works. Usecase: trigger a hdnler when a button is clicekd
on a window.

```java

@Value
public class Button {
    String title;

    //1. Create a signal
    ConsumingFireForgetSignal<Button> onClick = new ConsumingFireForgetSignal<>();

    public Button(final String title) {
        this.title = title;
    }

    void click() {
        //3. dispatch signal when you want to trigger observers
        onClick.dispatch(this);
    }
}

@Value
public class Window {
    Button b = new Button("Submit");

    public Window() {
        //2. Connect signal handlers 
        b.getOnClick().connect(button -> System.out.println("Button clicked: " + button.getTitle()));
    }
}

public class ApplicationTest {
    @Test
    void testW() {
        val window = new Window();
        window.getButton().click(); // Will print -> Button clicked: Submit
    }
}

```

## Signal

A signal is the global container/actor that remembers registered handlers. the `dispatch` method can be used to trigger
a signal.

### Signal Handlers

There are two types of Signal Handlers provided and used in the context fo different type of signals (see below).

* **SignalConsumer** - A signal handler that does not return result of computations performed on the incoming parameter.
  Equivalent to `Consumer<T>` in core java.
* **SignalHandler** - A signal handler that reutrns the results of computations performed on the provided parameter.
  This is equivalent to the `Function<T,R>` functional type in core java. Both these types can be coded as lambdas
  during usage.

### Response Combiners

Response combiners can `assimiate` data resulting from `SignalHandler` calls and can be used to accumulate data using
the `assimilate(param)` call and the final result is obtained form a final call to `result()`. The library provides
two `
ResponseCombiner` types:

* **ConsumingNoOpCombiner** - does nothing with the response
* **LastValueResponseCombiner** - Called in a chain, this wil lstore the last value it encounters

### Error Handlers

Error Handlers are used to handle exceptions (duh!!) thrown by the SignalHandler calls. The default consumer
called `LoggingTaskErrorHandler` logs the error and suppresses it. As a result, it makes sure that even if one handler
in a chain fails, the rest of the chain continues to execute.

## Type of signals

There are two basic type of signals:

1. Consuming Signal
2. Generating Signal

Both are implemented with various variants wrt how they call handlers. All classes priovide a default contrsuctor where
defaults (as mentioned below in respective sections) are set. Please use the corresponding provided builders (created
using build() static method call) to customise different aspects of the Signals.

### Consuming Signals

Consuming signals accept a `SignalConsumer` as handler and do not respond back with any responses. There are three types
of consuming signals depending on mode of handler dispatch. This represnts observer patterns more closely and should
suffice for most use-cases.

* **ConsumingSyncSignal** - A Consuming `Signal` that fires handlers in the same thread waits for them to complete. This
  is the closest to implementing a simple observer chain in your class. The calling function will obviously get blocked
  till all handlers execute in series. Choose this when your handlers are lightweight.
* **ConsumingParallelSignal** - A Consuming `Signal` that fires handlers in parallel and waits for them to complete.
  This will execute handlers in parallel. Execution order cannot be guaranteed. The calling function will block till all
  handlers have executed. Choose this if you need all handlers to complete and they are heavy thread-safe computations.
* **ConsumingFireForgetSignal** - A Consuming `Signal` that fires handlers in parallel and does not wait for their
  response. All handlers will be called on a threadpool (by default a single thread different from the callign thread).
  Use this when you do not need guarantees on execution completion of the handlers before moving on.

#### Defaults used

* **Exception Handler:** LoggingTaskErrorHandler
* **Response Combiner:** ConsumingNoOpCombiner

### Generating Signals

Generating signals accept handlers of type `SignalHandler` that returns response of processing. These results are
accumulated using the `ResponseCombiner` and the final result is returned to the caller as a return value of
the `dispatch(data)` call. There are two types of Generating Signals based on the mode of execution of handlers.

* **GeneratingSyncSignal** - A Generating `Signal` that fires handlers in the calling thread and waits for them to
  complete. It returns the response as obtained for a call to `ResponseCombiner.result()`.

* **GeneratingParallelSignal** - * A Generating `Signal` that fires handlers in parallel and waits for them to complete.
  It returns the response as obtained for a call to `ResponseCombiner.result()`.

Generating handlers can be used to implement decision points etc in complicated workflows where the main processing
halts for side-effects to complete and proceeds using the data generated by them

## Language Comatibility Level
Java 11

## License
Apache License 2.0

