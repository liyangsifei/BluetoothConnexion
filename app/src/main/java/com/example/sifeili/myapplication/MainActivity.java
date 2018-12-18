package com.example.sifeili.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.R.id.text1;

public class MainActivity extends AppCompatActivity implements TransMessageListener {

    //C'est l'UUID pour que les appareils peuvent s'identifier entre eux
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Les états de connexions
    private static final int STATE_LISTENING = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_CONNECTION_FAILED = 4;
    private static final int STATE_MESSAGE_RECEIVED = 5;

    //Pour charger les fragments
    private FragmentManager manager;
    private FragmentTransaction transaction;

    //numéro d'appareil, pour identifier si c'est l'appareil principal ou secondaire
    private int numeroDevice = 2;

    //options chosen from ChooseObjectiveFragment(get by TransMessageListener)
    private String objectiveChosen;
    private String objectiveReceived;
    private String objectiveTotal;
    private int readyInd = 0;
    private int readyIndOther = 0;

    //Les composantes de l'interface
    private Switch switchButton;
    private Button btnShowDevice, btnSearchDevice, btnListen;
    private ListView listViewDevicesConnected, listViewDevicesFound;
    private TextView tvStatus, tvFound, tvConnected;
    private LinearLayout linearLayout1, linearLayout2, linearLayout3, fragmentLayout;
    private ScrollView scrollView;

    //BluetoothAdapter pour prendre l'état de Bluetooth et faire les connexions
    private BluetoothAdapter bluetoothAdapter;

    //Un Thread pour envoyer les messages à l'autre appreil (La classe est à dessous de cette class)
    private SendReceive sendReceive;

    private ArrayAdapter devicesConnectedAdapter, devicesFoundAdapter;
    private List<String> listDevicesFound = new ArrayList<String>();
    private List<String> listDevicesBounded = new ArrayList<String>();
    private List<BluetoothDevice> devicesFound = new ArrayList<BluetoothDevice>();
    private List<BluetoothDevice> devicesConnected = new ArrayList<BluetoothDevice>();
    private BluetoothDevice device;

    //BroadcastReceiver pour recevoir les notifications envoyées par le système (l'état de Bluetooth) et faire différentes manipulations
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Searching",Toast.LENGTH_LONG).show();
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Détecter si l'appareil trouvé est jumulé avec cet appareil
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    listDevicesBounded.add("Name:" + device.getName() + " Address:" + device.getAddress() + " State: " + device.getBondState());
                    devicesConnected.add(device);
                    devicesConnectedAdapter.notifyDataSetChanged();
                }
                listDevicesFound.add("Name:" + device.getName() + " Address:" + device.getAddress() + " State: " + device.getBondState());
                devicesFound.add(device);
                devicesFoundAdapter.notifyDataSetChanged();

            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Toast.makeText(getApplicationContext(),"Search finished",Toast.LENGTH_LONG).show();
            }
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                //Les messages qui indique un chagment d'état de connexion
                case STATE_LISTENING :
                    tvStatus.setText("Listening...");
                    break;
                case STATE_CONNECTING :
                    tvStatus.setText("Connecting...");
                    break;
                case STATE_CONNECTED :
                    tvStatus.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED :
                    tvStatus.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED :
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);

                    /*
                        Identifier les types de message et exécuter différentes manipulations
                     */
                    //Si message est 9, cela indique que c'est une inivation
                    if(tempMsg.equals("9")) {
                        numeroDevice = 2;
                        objectiveChosen = null;
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        dialogBuilder.setTitle("Accept?");
                        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(MainActivity.this);
                                dialogBuilder2.setTitle("Accepted, choose activity");
                                dialogBuilder2.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendReceive.write("1".getBytes());
                                        toFragmentChooseObjective();
                                    }
                                });
                                AlertDialog dialog2 = dialogBuilder2.create();
                                dialog2.show();
                            }
                        });
                        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendReceive.write("0".getBytes());
                            }
                        });
                        AlertDialog dialog = dialogBuilder.create();
                        dialog.show();
                    //Si message comprend String "READY", c'est les deux appareils ont choisi leurs exercices
                    } else if(tempMsg.indexOf("READY")!=-1) {
                        objectiveTotal = tempMsg.substring(5);
                        objectiveReceived = null;
                        transGameOption();
                    //Si message est 0, c'est l'autre appareil a refusé son invitation
                    } else if(tempMsg.equals("0")) {
                    //Si message est 1, c'est l'autre appareil a accepté son invitation
                    } else if(tempMsg.equals("1")) {
                        //Pour reinitialiser les valeurs d'objetive choisi
                        objectiveChosen = null;
                        objectiveReceived = null;
                        objectiveTotal = null;
                        numeroDevice = 1;
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        dialogBuilder.setTitle("Accepted, choose activity");
                        dialogBuilder.setPositiveButton("Enter", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                toFragmentChooseObjective();
                            }
                        });
                        AlertDialog dialog = dialogBuilder.create();
                        dialog.show();
                    //Ces deux cas indique que l'autre utilisateur a choisi son exercice
                    } else if(tempMsg.indexOf("CODE")!=-1 && numeroDevice == 1) {
                        Toast.makeText(MainActivity.this, "Player2 have chosen activities", Toast.LENGTH_LONG).show();
                        objectiveReceived = tempMsg;
                    } else if (tempMsg.indexOf("CODE")!=-1 && numeroDevice == 2) {
                        Toast.makeText(MainActivity.this, "Player1 have chosen activities", Toast.LENGTH_LONG).show();
                        objectiveReceived = tempMsg;
                    //Ce cas indique que les deux appareils ont préparé pour le jeu
                    }  else if(tempMsg.equals("START")) {
                        readyIndOther = 1;
                        if(readyInd==1) {
                            startGame();
                        }
                    //Ce cas indique que l'autre appreil ont refusé le jeu
                    } else if(tempMsg.equals("CANCLED")) {
                        backToMain();
                    }
                    break;
                default :
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Prendre la défaut FragmentManager
        manager = getSupportFragmentManager();

        //Prendre le BluetoothAdapter par défault
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Créer les Adaptateur pour les listes des appareils
        devicesConnectedAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, text1, listDevicesBounded);
        devicesFoundAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, text1, listDevicesFound);

        /*
            Trouver les composantes dans l'interface
         */
        linearLayout1 = (LinearLayout) findViewById(R.id.main_linearlayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.sous1_linearlayout);
        linearLayout3 = (LinearLayout) findViewById(R.id.table_btn);
        fragmentLayout = (LinearLayout) findViewById(R.id.linear_premier);
        scrollView = (ScrollView) findViewById(R.id.scroll_devices_found);
        switchButton = (Switch) findViewById(R.id.switch_bluetooth);
        btnShowDevice = (Button) findViewById(R.id.btn_show);
        btnSearchDevice = (Button) findViewById(R.id.btn_search);
        btnListen = (Button) findViewById(R.id.btn_listen);
        listViewDevicesConnected = (ListView) findViewById(R.id.list_deviced_connected);
        listViewDevicesFound = (ListView) findViewById(R.id.list_deviced_found);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvFound = (TextView) findViewById(R.id.tv_found);
        tvConnected = (TextView) findViewById(R.id.tv_bounded);

        //Créer l'activité pour l'interrupteur de Bluetooth
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOn();
                } else {
                    turnOff();
                }
            }
        });

        //Créer l'activité pour le bouton de recherche l'appareil
        btnSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();
            }
        });

        //Créer l'activité pour le bouton d'écouter
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });

        //En cliquant sur ce bouton, il va exécuter la méthode d'afficher lui-même
        btnShowDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDevice();
            }
        });

        /*
            Ajouter les types de notification à écouter au filtre
         */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver,filter);


        /*
            Cette List View prend les appareils trouvé par le Bluetooth et les ajoute dans son View
            Quand un item (une ligne) est cliqué, il va créer une
                cela indique que l'utilisateur veut faire une connexion à cette appareil choisi donc ce qui clique sur cette liste
                est le Socket Client (le Socket
        */
        listViewDevicesFound.setAdapter(devicesFoundAdapter);
        listViewDevicesFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Annuler si le Bluetooth est en recherche
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                ClientClass clientClass = new ClientClass(devicesFound.get(position));
                clientClass.start();
                //Mise à jour le statut de connexion
                tvStatus.setText("Connecting");
                //Mise à jour la liste des appareils connecté
                devicesConnectedAdapter.notifyDataSetChanged();
            }
        });

        /*
            La listes des appareils pour ceux-ci déjà connecté à cet appareil
        */
        listViewDevicesConnected.setAdapter(devicesConnectedAdapter);
        listViewDevicesConnected.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                /*
                    Si l'appareil déjà connecté avec cette application, la variable sendReceive n'aura pas être null, et il va
                        envoyer une invitation directement (Sinon voir else)
                 */
                if(sendReceive!=null) {
                    dialogBuilder.setTitle("Invite?");
                    dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String s = (String) devicesConnectedAdapter.getItem(position);

                            String address = s.substring(s.indexOf(":") + 1).trim();
                            if (bluetoothAdapter.isDiscovering()) {
                                bluetoothAdapter.cancelDiscovery();
                            }
                            try {
                                sendReceive.write("9".getBytes());
                            } catch (Exception e) {
                                    ClientClass clientClass = new ClientClass(devicesConnected.get(position));
                                    clientClass.start();
                            }
                        }
                    });
                    dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                } else {
                    /*
                        Si l'appareil n'a pas encore fait une connexion dans cette application, il va créer une classe Client pour
                            connecter à cet appareil choisi
                     */

                    ClientClass clientClass = new ClientClass(devicesConnected.get(position));
                    clientClass.start();
                }
            }
        });
    }

    /*
        OnStart c'est chaque fois le view s'affiche, il exécute ces méthode là pour vérifier le statut de Bluetooth
     */
    @Override
    protected void onStart() {

        super.onStart();
        if(bluetoothAdapter.isEnabled()) {
            switchButton.setChecked(true);
        } else {
            switchButton.setChecked(false);
        }
    }

    //Méthode pour ouvrir le Bluetooth
    public void turnOn() {
        if(!bluetoothAdapter.isEnabled()) {
            Intent inTurnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(inTurnOn, 0);
            Toast.makeText(getApplicationContext(),"Bluetooth on", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(),"Bluetooth already on", Toast.LENGTH_LONG).show();
        }
    }

    //Méthode pour fermer le Bluetooth
    public void turnOff() {
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Bluetooth off", Toast.LENGTH_LONG).show();
    }

    //Méthode pour chercher les autres appareils
    public void searchDevices() {
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        //Chaque fois il fait des recherche, la liste va être
        listDevicesFound.clear();
        devicesFoundAdapter.notifyDataSetChanged();
        listDevicesBounded.clear();
        devicesConnectedAdapter.notifyDataSetChanged();
        bluetoothAdapter.startDiscovery();
    }

    //Méthode pour mettre l'appareil lui-même trouvé par les autres appareils
    public void showDevice() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /*
        Méthode pour le Socket(appareil) Serveur de créer un Thread pour écouter les connextions des autres appareils
     */
    public void listen() {
        ServerClass serverClass = new ServerClass();
        serverClass.start();
    }

    /*
        Quand une invitation est accepté, les deux appareils exécutent cette méthode pour aller à Fragemnt de choisir leurs objets
     */
    public void toFragmentChooseObjective() {

        ChooseObjectiveFragment cof = new ChooseObjectiveFragment();
        Bundle bundle = new Bundle();
        cof.setArguments(bundle);

        transaction = manager.beginTransaction();
        transaction.add(R.id.linear_premier, cof);
        transaction.commit();
        LinearLayout l;
        l = (LinearLayout) findViewById(R.id.main_linearlayout);
        l.setWeightSum(5);

        /*
            C'est pour cacher tous les composantes dans l'interface MainActivity, laisser l'espace pour afficher le Fragment
         */
        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        switchButton.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.GONE);
        btnSearchDevice.setVisibility(View.GONE);
        btnShowDevice.setVisibility(View.GONE);
        btnListen.setVisibility(View.GONE);
        tvConnected.setVisibility(View.GONE);
        listViewDevicesConnected.setVisibility(View.GONE);
        tvFound.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        listViewDevicesFound.setVisibility(View.GONE);
        fragmentLayout.setVisibility(View.VISIBLE);
    }

    /*
        Méthode pour recevoir les options que l'utilisateur a choisi et executer la méthode d'entrer au autre fragment
     */
    @Override
    public void sendGameOption(String str) {
        objectiveChosen = str;
        if(objectiveReceived == null) {
            sendReceive.write(objectiveChosen.toString().getBytes());
        } else if (objectiveReceived != null) {
            objectiveTotal = "READY" + objectiveChosen + objectiveReceived;
            sendReceive.write(objectiveTotal.toString().getBytes());
            objectiveTotal = objectiveTotal.substring(5);

            objectiveReceived = null;
            transGameOption();
        }
    }

    /*
        Méthode pour recevoir le résultat que l'utilisateur choisi d'entrer au jeu ou d'abandonner
     */
    @Override
    public void sendBeginGameMsg(int i) {
        if(i==1) {
            sendReceive.write("START".getBytes());
            readyInd = 1;
        } else if(i==0) {
            sendReceive.write("CANCLED".getBytes());
            backToMain();
        }
        if(readyIndOther == 1) {
            startGame();
        }
    }

    /*
        Méthode pour charger le Fragment GameFragment
     */
    public void startGame() {
        GameFragment fragment = new GameFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        transaction = manager.beginTransaction();
        transaction.replace(R.id.linear_premier, fragment);
        transaction.commit();
    }

    /*
        Quand l'exercice est annulé par un des deux utilisateurs, les deux appareils exécutent cette méthode pour retourner dans
            l'interface de MainActivity, alors toutes les composantes de MainActivity sont visible
     */
    public void backToMain() {
        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);
        switchButton.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        linearLayout3.setVisibility(View.VISIBLE);
        btnSearchDevice.setVisibility(View.VISIBLE);
        btnShowDevice.setVisibility(View.VISIBLE);
        btnListen.setVisibility(View.VISIBLE);
        tvConnected.setVisibility(View.VISIBLE);
        listViewDevicesConnected.setVisibility(View.VISIBLE);
        tvFound.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        listViewDevicesFound.setVisibility(View.VISIBLE);
        fragmentLayout.setVisibility(View.GONE);
    }

    /*
        Méthode pour charger le Fragment ExerciseDetailsFragment et envoyer la variable qui indique c'est quel exercice à ce Fragment
     */
    public void transGameOption() {
        ExerciseDetailsFragment fragment = new ExerciseDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("objective",objectiveTotal);
        fragment.setArguments(bundle);
        transaction = manager.beginTransaction();
        transaction.replace(R.id.linear_premier, fragment);
        transaction.commit();
    }

    /*
        Le Thread de Socket Serveur pour recevoir les connexions envoyé par autre Socket Client
     */
    private class ServerClass extends Thread {

        private BluetoothServerSocket serverSocket;

        public ServerClass() {

            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyApplication", MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    break;
                }
                if(socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    /*
        Le Thread pour le Socket Client de faire la connexion à autre Socket Serveur
     */
    private class ClientClass extends Thread {

        private BluetoothSocket socket;
        private BluetoothDevice device;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try{
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
                e.printStackTrace();
            }
        }

    }

    /*
        Le Thread pour envoyer les messages (par Bytes)
     */
    public class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempInput = null;
            OutputStream tempOutput = null;

            try {
                tempInput = bluetoothSocket.getInputStream();
                tempOutput = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempInput;
            outputStream = tempOutput;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
