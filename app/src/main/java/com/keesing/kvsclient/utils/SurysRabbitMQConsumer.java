package com.keesing.kvsclient.utils;

import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SurysRabbitMQConsumer implements Consumer {

    private final String TAG = SurysRabbitMQConsumer.class.getName();
    private Channel channel;
    private final DataReceiver<String> resultsListener;

    public SurysRabbitMQConsumer(DataReceiver<String> resultsListener) {
        this.resultsListener = resultsListener;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Called when the consumer is registered by a call to any of the
     * {@link Channel#basicConsume} methods.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     */
    @Override
    public void handleConsumeOk(String consumerTag) {
        Log.i(TAG, "handleConsumeOk " + consumerTag);
    }

    /**
     * Called when the consumer is cancelled by a call to {@link Channel#basicCancel}.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     */
    @Override
    public void handleCancelOk(String consumerTag) {
        Log.i(TAG, "handleCancelOk " + consumerTag);
    }

    /**
     * Called when the consumer is cancelled for reasons <i>other than</i> by a call to
     * {@link Channel#basicCancel}. For example, the queue has been deleted.
     * See {@link #handleCancelOk} for notification of consumer
     * cancellation due to {@link Channel#basicCancel}.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @throws IOException
     */
    @Override
    public void handleCancel(String consumerTag) throws IOException {
        Log.i(TAG, "handleCancel " + consumerTag);
    }

    /**
     * Called when either the channel or the underlying connection has been shut down.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param sig         a {@link ShutdownSignalException} indicating the reason for the shut down
     */
    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        Log.i(TAG, "handleShutdownSignal " + consumerTag);
    }

    /**
     * Called when a <code><b>basic.recover-ok</b></code> is received
     * in reply to a <code><b>basic.recover</b></code>. All messages
     * received before this is invoked that haven't been <i>ack</i>'ed will be
     * re-delivered. All messages received afterwards won't be.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     */
    @Override
    public void handleRecoverOk(String consumerTag) {
        Log.i(TAG, "handleRecoverOk " + consumerTag);
    }

    /**
     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param envelope    packaging data for the message
     * @param properties  content header data for the message
     * @param body        the message body (opaque, client-specific byte array)
     * @throws IOException if the consumer encounters an I/O error while processing the message
     * @see Envelope
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Log.i(TAG, "handleDelivery " + consumerTag);
        this.channel.basicAck(envelope.getDeliveryTag(), false);
        String json = new String(body);
        this.resultsListener.run(json);
    }
}
