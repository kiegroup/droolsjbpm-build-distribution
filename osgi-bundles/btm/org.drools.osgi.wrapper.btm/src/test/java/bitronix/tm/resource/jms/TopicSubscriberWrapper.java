/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bitronix.tm.resource.jms;

import javax.jms.TopicSubscriber;
import javax.jms.Topic;
import javax.jms.JMSException;

/**
 * {@link TopicSubscriber} wrapper that adds XA enlistment semantics.
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class TopicSubscriberWrapper extends MessageConsumerWrapper implements TopicSubscriber {

    public TopicSubscriberWrapper(TopicSubscriber topicSubscriber, DualSessionWrapper session, PoolingConnectionFactory poolingConnectionFactory) {
        super(topicSubscriber, session, poolingConnectionFactory);
    }

    public Topic getTopic() throws JMSException {
        return ((TopicSubscriber) getMessageConsumer()).getTopic();
    }

    public boolean getNoLocal() throws JMSException {
        return ((TopicSubscriber) getMessageConsumer()).getNoLocal();
    }

    public String toString() {
        return "a TopicSubscriberWrapper of " + session;
    }

}
