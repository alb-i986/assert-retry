package me.alb_i986.testing.assertions.retry;

import org.hamcrest.SelfDescribing;

public interface WaitStrategy extends SelfDescribing {

    void waitt() throws Exception;
}
