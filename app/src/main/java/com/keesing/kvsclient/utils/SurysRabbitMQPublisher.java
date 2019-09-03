package com.keesing.kvsclient.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SurysRabbitMQPublisher extends AsyncTask<String, Integer, Void> {
    private final String imagePath;//  {
    private final DataReceiver consumer;
    private final String TAG = SurysRabbitMQPublisher.class.getName();

    private byte[] loadImage(String imgSrc) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imgSrc, options);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    public SurysRabbitMQPublisher(String imagePath, DataReceiver consumer) {

        this.imagePath = imagePath;
        this.consumer = consumer;
    }

    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // "guest"/"guest" by default, limited to localhost connections
            factory.setUsername("keesing");
            factory.setPassword("test");
            factory.setVirtualHost("/");
            factory.setHost("51.158.171.147");
            factory.setPort(5672);

            Connection conn = factory.newConnection();
            final Channel channel = conn.createChannel();

            channel.queueDeclare("task_queue_mrz", true, false, false, null);
            // channel.queuePurge("task_queue_mrz");
            String base64 = Base64.encodeToString(loadImage(this.imagePath), Base64.DEFAULT);

            String id = java.util.UUID.randomUUID().toString();
            AMQP.BasicProperties basicProps = new AMQP.BasicProperties().builder().deliveryMode(2).build();

            JSONObject jo = new JSONObject();
            jo.put("name", this.imagePath.substring(this.imagePath.lastIndexOf(File.separatorChar)));
            jo.put("bytes", base64.getBytes());
            jo.put("id", id);
            String json = jo.toString();

            channel.basicPublish("", "task_queue_mrz", basicProps, json.getBytes());
            // channel.basicPublish("", "task_queue_mrz", basicProps, "finished".getBytes());

            channel.queueDeclare("result_MRZ_" + id, false, false, true, null);

            channel.basicConsume("result_MRZ_" + id, false, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties props,
                                           byte[] body
                ) throws IOException {

                    Log.i(TAG, "handleDelivery " + consumerTag);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    String json = new String(body);

                }
            });


            //new SurysRabbitMQConsumer(channel, this.consumer));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param strings The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(String... strings) {

        run();
        return null;
    }
}
