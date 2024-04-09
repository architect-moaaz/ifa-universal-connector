package org.acme.customprocessor;

import com.mongodb.kafka.connect.sink.MongoSinkTopicConfig;
import com.mongodb.kafka.connect.sink.converter.SinkDocument;
import com.mongodb.kafka.connect.sink.processor.PostProcessor;
import org.apache.kafka.connect.sink.SinkRecord;

public class MongoDBPostProcessor extends PostProcessor {

    public MongoDBPostProcessor(MongoSinkTopicConfig config) {
        super(config);
    }

    @Override
    public void process(SinkDocument sinkDocument, SinkRecord sinkRecord) {

    }
}
