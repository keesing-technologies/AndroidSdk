package com.keesing.kvsclient.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// import android.util.Base64;


public class SurysRabbitMQPublisher extends AsyncTask<String, Integer, Void> {
    private final String imagePath;//  {
    private final SurysRabbitMQConsumer consumer;
    private final String TAG = SurysRabbitMQPublisher.class.getName();
    private ProgressDialog progressDialog = null;

    private byte[] loadImage(String imgSrc) throws IOException {

        File file = new File(imgSrc);
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[(int) file.length()];
        inputStream.read(buffer, 0, buffer.length);
        inputStream.close();
        return buffer;

        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imgSrc, options);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;*/
    }

    public SurysRabbitMQPublisher(Context context, String imagePath, SurysRabbitMQConsumer consumer) {

        this.imagePath = imagePath;
        this.consumer = consumer;

        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SurysRabbitMQPublisher.this.cancel(true);
            }
        });
    }

    public void run(String... params) {
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

            Base64 b64 = new Base64();
            String img1Base64 =  params.length >= 1 ? params[0] :
                    new String(b64.encode(loadImage(this.imagePath)));

            String img2Base64 = params.length == 2 ? params[1] : null;

            String id = java.util.UUID.randomUUID().toString();
            AMQP.BasicProperties basicProps = new AMQP.BasicProperties().builder().deliveryMode(2).build();

            if(!img1Base64.startsWith("/9"))
                img1Base64 = img1Base64.substring(img1Base64.indexOf(",/9") + 1);

            JSONObject img1Json = new JSONObject();
            img1Json.put("name", this.imagePath.length() != 0 ? this.imagePath.substring(this.imagePath.lastIndexOf(File.separatorChar)) : "S_00123.jpg");
            img1Json.put("bytes", img1Base64);
            img1Json.put("id", id);

            channel.basicPublish("", "task_queue_mrz", basicProps, img1Json.toString().getBytes());

            if( img2Base64 != null)
            {
                if(!img2Base64.startsWith("/9"))
                    img2Base64 = img2Base64.substring(img2Base64.indexOf(",/9") + 1);

                JSONObject img2Json = new JSONObject();
                img2Json.put("name", this.imagePath.length() != 0 ? this.imagePath.substring(this.imagePath.lastIndexOf(File.separatorChar)) : "S_00123.jpg");
                img2Json.put("bytes", img2Base64);
                img2Json.put("id", id);

                channel.basicPublish("", "task_queue_mrz", basicProps, img2Json.toString().getBytes());
            }

            // channel.basicPublish("", "task_queue_mrz", basicProps, "finished".getBytes());

            channel.queueDeclare("result_MRZ_" + id, false, false, true, null);

            this.consumer.setChannel(channel);
            channel.basicConsume("result_MRZ_" + id, false, this.consumer);
            /*channel.basicConsume("result_MRZ_" + id, false, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties props,
                                           byte[] body
                ) throws IOException {

                    Log.i(TAG, "handleDelivery " + consumerTag);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    String json = new String(body);
                    Log.i(TAG, json);
                    SurysRabbitMQPublisher.this.consumer.run(json);
                }
            });*/


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

        run(strings);
        return null;
    }

    @Override
    protected void onPreExecute() {
        this.progressDialog.setMessage("Extracting MRZ, Please wait...");
        this.progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(this.progressDialog.isShowing()){
            this.progressDialog.dismiss();
        }
    }
}
