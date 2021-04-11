# Assert Retry

An extension to JUnit/Hamcrest providing *assertions with tolerance*, featuring a **retry** mechanism.


## Motivation

AFAIK there are already two libraries out there resolving the same problem this library is addressing:
 
- [Awaitility](https://github.com/awaitility/awaitility)
- [guava-retrying](https://github.com/rholder/guava-retrying)

Awaitility in particular looks pretty good indeed.

The added value of `assert-retry` is that it is integrated with Hamcrest's `assertThat` assertions.

What I love about Hamcrest assertions is the valuable feedback they provide to the user when they fail.


## Example of usage

Say that we have a JMS queue, and we need to verify that a message with body "expected content" is published on the queue.
Given the async nature of the system, we need to employ a bit of tolerance in our assertions.

    import static me.alb_i986.retry.hamcrest.RetryMatcher.eventually;
    import static org.hamcrest.MatcherAssert.assertThat;
    import static org.hamcrest.Matchers.containsString;
      
    MessageConsumer consumer = session.createConsumer(queue);
    connection.start();
    Supplier<String> messageText = new Supplier<>() {
        @Override
        public String get() throws JMSException {
            TextMessage m = (TextMessage) consumer.receiveNoWait();  // polling for messages, non-blocking
            return m == null ? null : m.getText();
        }
    };
    assertThat(messageText, eventually(containsString("expected content"))
            .within(60, ChronoUnit.SECONDS)
            .sleepForSeconds(5)
            .retryOnException(JMSException.class));

The first few lines set up the Supplier of actual values, which will poll the message queue for messages.

Then we have our assertion.
In this example it is asserting that the expected text message will be received within 60 seconds.
After each failing attempt, it will wait for 5s, and then it will try again.

If `consumer.receiveNoWait()` throws a `JMSException`, the assertion will be re-tried,
as if it returned a non-matching value.

Finally, the assertion will timeout after 60s, and an AssertionError similar to the following will be thrown:

       java.lang.AssertRetryError:
       Expected: supplied value to *eventually* match a string containing "expected content" within 60s
           but: The timeout expired and none of the actual values matched
                Actual results (in order of appearance):
                 1. "some content"
                 2. thrown javax.jms.MessageFormatException: Blah blah
                         at com.example.MyJMSTest.test()
                         [..]
                 3. "some other content"

For more info, please check the javadoc of `RetryMatcher#eventually`.
