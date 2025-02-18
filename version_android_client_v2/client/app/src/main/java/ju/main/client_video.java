/**
 * -------------------------------------------------------------------
 * Nom du fichier : client_video.java
 * Auteur         : BEAL JULIEN
 * Version        : 1.0
 * Date           : 11/02/2025
 * Description    : Code affichage client version android
 * -------------------------------------------------------------------
 * © 2025 BEAL JULIEN - Tous droits réservés
 */

package ju.main;

import static android.content.ContentValues.TAG;

import ju.util.SystemUiHider;
import ju.util.thread_reception_image;
import ju.util.thread_reception_string;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

public class client_video extends Activity {


    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV 4.10 chargé avec succès !");
            // Par exemple, si vous utilisez une vue caméra :
            // mOpenCvCameraView.enableView();
        } else {
            Log.e(TAG, "Echec du chargement d'OpenCV !");
            // Gérer l'erreur d'initialisation
        }
    }

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    private SystemUiHider mSystemUiHider;
    private ImageView fullscreenContent;
    private Button button1, button2, button3, button4;

    Mat imageRecu;
    Bitmap bitmapImage = null;
    String address_local_str;
    final int[] port = {55000, 55001, 55002}; // Définition des ports UDP
    final String address = "172.29.41.9"; // Définition des adresses IP
    final String address_broadcast = "172.29.255.255";
    String text = "";
    final byte[] data = new byte[65536];
    int previousTraitement = 0;
    int currentTraitement = 0;
    Mat blackImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_video);
        
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV 4.10 chargé avec succès !");
            // Par exemple, si vous utilisez une vue caméra :
            // mOpenCvCameraView.enableView();
        } else {
            Log.e(TAG, "Echec du chargement d'OpenCV !");
            // Gérer l'erreur d'initialisation
        }

        DatagramSocket socket_image = null;
        DatagramSocket socket_cmd = null;
        DatagramPacket packet = null;

        InetAddress address_local = null;

        try {
            // Obtenir l'adresse IP locale
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        String ip = inetAddress.getHostAddress();
                        if (ip.startsWith("172.29")) {
                            address_local = inetAddress;
                            address_local_str = ip;
                            break;
                        }
                    }
                }
                if (address_local != null) {
                    break;
                }
            }

            if (address_local == null) {
                throw new Exception("Aucune adresse IP locale valide trouvée.");
            }

            // Initialisation du socket UDP
            socket_image = new DatagramSocket(port[1]);
            socket_cmd = new DatagramSocket(port[2]);
            packet = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageRecu = new Mat();
        Mat dermiereImageValide = null;
        Mat Image_a_afficher = new Mat(), dermiereImageValide_resizedImage = new Mat();

        long currentTime, previousTime = System.nanoTime();
        double intervalInSeconds, fps;

        blackImage = new Mat(360, 640, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Imgproc.putText(
                blackImage,
                "START",
                new org.opencv.core.Point((blackImage.cols() - Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).width) / 2,
                        (blackImage.rows() + Imgproc.getTextSize("START", Imgproc.FONT_HERSHEY_SIMPLEX, 2.0, 3, null).height) / 2),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                2.0,
                new Scalar(255, 255, 255),
                3
        );

        final thread_reception_image reception = new thread_reception_image("client_UDP_image", socket_image, imageRecu);
        reception.start();

        thread_reception_string cmd = new thread_reception_string("traitement_UDP_String", socket_cmd);
        cmd.start();

        // Thread pour envoyer l'adresse IP locale toutes les 3 minutes
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("boucle d'afk");
                while (true) {
                    try {
                        sendTextUDP("address#" + address_local_str + "?time#" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), address_broadcast, port[2]);
                        Thread.sleep(30000); // attendre 30 secondes
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        fullscreenContent = (ImageView) findViewById(R.id.fullscreen_content);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        // Set up an instance of SystemUiHider to control the system UI for this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, fullscreenContent, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    // If the ViewPropertyAnimator API is available (Honeycomb MR2 and later), use it to animate the in-layout UI controls at the bottom of the screen.
                    if (mControlsHeight == 0) {
                        mControlsHeight = controlsView.getHeight();
                    }
                    if (mShortAnimTime == 0) {
                        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                    }
                    controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
                } else {
                    // If the ViewPropertyAnimator APIs aren't available, simply show or hide the in-layout UI controls.
                    controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }

                if (visible && AUTO_HIDE) {
                    // Schedule a hide().
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Set up button click listeners
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                String formattedDate = sdf.format(calendar.getTime());
                switch (v.getId()) {
                    case R.id.button1:
                        currentTraitement = 0 ;
                        break;
                    case R.id.button2:
                        currentTraitement = 1 ;
                        break;
                    case R.id.button3:
                        currentTraitement = 2 ;
                        break;
                    case R.id.button4:
                        currentTraitement = 3 ;
                        break;
                }
                try {
                    text = "address#" + address_local_str + ":" + port[1] + "?traitement#" + currentTraitement + "?time#" + formattedDate ;
                    sendTextUDP(text, address_broadcast, port[2]);
                    previousTraitement = currentTraitement; // Mettre à jour l'état précédent
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        button1.setOnClickListener(buttonClickListener);
        button2.setOnClickListener(buttonClickListener);
        button3.setOnClickListener(buttonClickListener);
        button4.setOnClickListener(buttonClickListener); // Ensure this line is not commented out

        // Upon interacting with UI controls, delay any scheduled hide() operations to prevent the jarring behavior of controls going away while interacting with the UI.
        findViewById(R.id.fullscreen_content_controls).setOnTouchListener(mDelayHideTouchListener);

        // Boucle principale pour afficher l'image reçue
        new Thread(new Runnable() {
            @Override
            public void run() {
                Mat dermiereImageValide = null;
                Mat Image_a_afficher = new Mat(), dermiereImageValide_resizedImage = new Mat();
                long currentTime, previousTime = System.nanoTime();
                double intervalInSeconds, fps;

                while (true) {
                    imageRecu = reception.getImageRecu();

                    if (imageRecu == null) {
                        if (dermiereImageValide != null) {
                            Image_a_afficher = dermiereImageValide_resizedImage;
                        } else {
                            Image_a_afficher = blackImage;
                        }
                    } else {
                        dermiereImageValide = imageRecu ;
                        currentTime = System.nanoTime();
                        intervalInSeconds = (currentTime - previousTime) / 1000000000.0;
                        fps = 1.0 / intervalInSeconds;
                        Imgproc.putText(dermiereImageValide, String.format("FPS: %.0f", fps), new org.opencv.core.Point(10, 60), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
                        previousTime = currentTime;

                        Size displayFrameHalfSize = new Size(imageRecu.width() / 2, imageRecu.height() / 2);
                        Imgproc.resize(dermiereImageValide, dermiereImageValide_resizedImage, displayFrameHalfSize);
                        Image_a_afficher = dermiereImageValide_resizedImage;
                    }

                    // Afficher l'image dans la fenêtre
                    try {
                        bitmapImage = byteArrayToBitmap(encodeImageToJPEG(Image_a_afficher, 100));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fullscreenContent.setImageBitmap(bitmapImage);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private byte[] encodeImageToJPEG(Mat image, int quality) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality));
        return matOfByte.toArray();
    }

    private static Bitmap byteArrayToBitmap(byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        return BitmapFactory.decodeStream(bis);
    }

    private void sendTextUDP(String data, String address, int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(); // Crée un socket UDP
            InetAddress ipAddress = InetAddress.getByName(address); // Résolution de l'adresse IP

            byte[] buffer = data.getBytes(); // Convertir le texte en tableau d'octets

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
            socket.send(packet); // Envoie du paquet UDP

            System.out.println("Données envoyées à " + address + ":" + port);
        }catch (IOException e ){
            System.out.println("socket null ");
        }
        finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Ferme le socket proprement
            }
        }
    }
}