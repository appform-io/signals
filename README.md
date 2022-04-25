# Signals

[![Build](https://github.com/appform-io/signals/actions/workflows/sonarcloud-checks.yml/badge.svg?branch=master)](https://github.com/appform-io/signals/actions/workflows/sonarcloud-checks.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=appform-io_signals&metric=coverage)](https://sonarcloud.io/dashboard?id=appform-io_signals)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=appform-io_signals&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=appform-io_signals)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=appform-io_signals&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=appform-io_signals)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=appform-io_signals&metric=security_rating)](https://sonarcloud.io/dashboard?id=appform-io_signals)

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
  <version>1.3</version>
</dependency>
```

The following section is some random java code to show how it works. Usecase: trigger a handler when a button is clicked
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

There are two types of Signal Handlers provided and used in the context of different type of signals (see below).

* **SignalConsumer** - A signal handler that does not return the result of computations performed on the incoming parameter.
  Equivalent to `Consumer<T>` in core java.
* **SignalHandler** - A signal handler that returns the results of computations performed on the provided parameter.
  This is equivalent to the `Function<T,R>` functional type in core java. Both these types can be coded as lambdas
  during usage.

### Response Combiners

Response combiners can `assimilate` data resulting from `SignalHandler` calls and can be used to accumulate data using
the `assimilate(param)` call and the final result is obtained form a final call to `result()`. The library provides
two `
ResponseCombiner` types:

* **ConsumingNoOpCombiner** - does nothing with the response
* **LastValueResponseCombiner** - Called in a chain, this will store the last value it encounters

### Error Handlers

Error Handlers are used to handle exceptions (duh!!) thrown by the SignalHandler calls. The default consumer
called `LoggingTaskErrorHandler` logs the error and suppresses it. As a result, it makes sure that even if one handler
in a chain fails, the rest of the chain continues to execute.

## Type of signals

There are two basic type of signals:

1. Consuming Signal
2. Generating Signal

Both are implemented with various variants wrt how they call handlers. All classes provide a default constructor where
defaults (as mentioned below in respective sections) are set. Please use the corresponding provided builders (created
using build() static method call) to customise different aspects of the Signals.

### Consuming Signals

Consuming signals accept a `SignalConsumer` as handler and do not respond back with any responses. There are three types
of consuming signals depending on the mode of handler dispatch. This represents observer patterns more closely and should suffice for most use-cases.

* **ConsumingSyncSignal** - A Consuming `Signal` that fires handlers in the same thread waits for them to complete. This
  is the closest to implementing a simple observer chain in your class. The calling function will obviously get blocked
  till all handlers execute in series. Choose this when your handlers are lightweight.
* **ConsumingParallelSignal** - A Consuming `Signal` that fires handlers in parallel and waits for them to complete.
  This will execute handlers in parallel. Execution order cannot be guaranteed. The calling function will block till all
  handlers have executed. Choose this if you need all handlers to complete and they are heavy thread-safe computations.
* **ConsumingFireForgetSignal** - A Consuming `Signal` that fires handlers in parallel and does not wait for their
  response. All handlers will be called on a thread-pool (by default a single thread different from the calling thread).
  Use this when you do not need guarantees on execution completion of the handlers before moving on.
* **ScheduledSignal** - A consuming `Signal` where the handler is called at specified intervals. All handlers will be
  called on a thread-pool (by default a single thread different from the calling thread). Use this to setup regular
  refresh jobs etc.

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
halts for side effects to complete and proceeds using the data generated by them

### Named Handlers

There are use cases, where you might want to register handlers to a signal and de-register them later when you are no
longer interested in listening to dispatches.

To handle this, new methods `connect([groupId], name, handler)` and `disconnect([groupId], name)`
methods have been introduced. Connect and disconnect is available on all signal types.

## Language Compatibility Level

Java 8

## License

Apache License 2.0

