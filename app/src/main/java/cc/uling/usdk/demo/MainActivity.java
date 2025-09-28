package cc.uling.usdk.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cc.uling.usdk.USDK;
import cc.uling.usdk.abs.ActionRunnable;
import cc.uling.usdk.board.UBoard;
import cc.uling.usdk.board.mdb.para.BRReplyPara;
import cc.uling.usdk.board.mdb.para.CBReplyPara;
import cc.uling.usdk.board.mdb.para.HCReplyPara;
import cc.uling.usdk.board.mdb.para.IPReplyPara;
import cc.uling.usdk.board.mdb.para.MPReplyPara;
import cc.uling.usdk.board.mdb.para.PMReplyPara;
import cc.uling.usdk.board.mdb.para.PayReplyPara;
import cc.uling.usdk.board.mdb.para.ResultReplyPara;
import cc.uling.usdk.board.mdb.para.SVReplyPara;
import cc.uling.usdk.board.wz.para.AOReplyPara;
import cc.uling.usdk.board.wz.para.ASReplyPara;
import cc.uling.usdk.board.wz.para.BSReplyPara;
import cc.uling.usdk.board.wz.para.CXReplyPara;
import cc.uling.usdk.board.wz.para.CYReplyPara;
import cc.uling.usdk.board.wz.para.DCReplyPara;
import cc.uling.usdk.board.wz.para.DSReplyPara;
import cc.uling.usdk.board.wz.para.IOReplyPara;
import cc.uling.usdk.board.wz.para.MTReplyPara;
import cc.uling.usdk.board.wz.para.PXReplyPara;
import cc.uling.usdk.board.wz.para.PYReplyPara;
import cc.uling.usdk.board.wz.para.RMReplyPara;
import cc.uling.usdk.board.wz.para.ResetReplyPara;
import cc.uling.usdk.board.wz.para.SReplyPara;
import cc.uling.usdk.board.wz.para.SSReplyPara;
import cc.uling.usdk.board.wz.para.SXPReplyPara;
import cc.uling.usdk.board.wz.para.SYPReplyPara;
import cc.uling.usdk.board.wz.para.TXReplyPara;
import cc.uling.usdk.board.wz.para.TYReplyPara;
import cc.uling.usdk.board.wz.para.TempReplyPara;
import cc.uling.usdk.board.wz.para.XPReplyPara;
import cc.uling.usdk.board.wz.para.XSReplyPara;
import cc.uling.usdk.board.wz.para.XioReplyPara;
import cc.uling.usdk.board.wz.para.YPReplyPara;
import cc.uling.usdk.board.wz.para.YSReplyPara;
import cc.uling.usdk.board.wz.para.YioReplyPara;
import cc.uling.usdk.constants.CodeUtil;
import cc.uling.usdk.constants.ErrorConst;
import cc.uling.usdk.mgr.LogManager;
import cc.uling.usdk.mgr.TPoolManager;
import cc.uling.usdk.para.BaseClsPara;

public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private UBoard mBoard;
    private String commid = "/dev/ttyS1";

    private EditText etCommid;
    private ListView listView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mLogs = new ArrayList<>();

    private EditText etNo;
    private EditText etMul;
    private EditText etMulBalance;
    private EditText etAddr, etWayNo, etWayType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initLogView();

        etCommid = findViewById(R.id.et_commid);
        etCommid.setText(commid);

        openCom();
        clickView(R.id.btn_open, view -> {
            String string = etCommid.getText().toString();
            if (!TextUtils.isEmpty(string)) {
                mBoard = USDK.getInstance().create(string);
                int ret = mBoard.EF_OpenDev(string, 9600);
                addLog(ret == ErrorConst.MDB_ERR_NO_ERR ? "Successfully opened" : "Failed to open" + " serial port " + string);
            } else {
                addLog("The serial port name cannot be empty.");
            }
        });

        clickView(R.id.btn_close, view -> {
            if (checkOpen()) {
                mBoard.EF_CloseDev();
                addLog("The serial port is closed.");
            }
        });

        clickView(R.id.btn_hw_config, view -> {
            if (checkOpen()) {
                addLog("Check hardware configuration.");
                HCReplyPara para = new HCReplyPara();
                mBoard.readHardwareConfig(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("version:" + para.getVersion() + ",Coin:" + para.isWithCoin() + ",Cash:" + para.isWithCash() + ",POS:" + para.isWithPOS() + ",Pulse:" + para.isWithPulse() + ",IDCard" + para.isWithIdentify());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_version, view -> {
            if (checkOpen()) {
                addLog("Check software version.");
                SVReplyPara para = new SVReplyPara();
                mBoard.getSoftwareVersion(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("version:" + para.getVersion());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_min_pay_amount, view -> {
            if (checkOpen()) {
                addLog("Read the minimum payment unit amount supported by the device.");
                MPReplyPara para = new MPReplyPara();
                mBoard.getMinPayoutAmount(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Minimum payment amount:" + para.getValue() + ",Decimal places:" + para.getDecimal());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_pay_amount, view -> {
            if (checkOpen()) {
                addLog("Check the amount received.");
                PMReplyPara para = new PMReplyPara();
                mBoard.getPayAmount(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    String name = "";
                    if (para.getPayType() == 0) {
                        name = "Coin";
                    } else if (para.getPayType() == 1) {
                        name = "Cash";
                    } else if (para.getPayType() == 2) {
                        name = "POS";
                    }
                    addLog("Payment method is in " + name + "，and the amount received is " + para.getMultiple() + " times the minimum unit.");
                } else {
                    addError(para);
                }
            }
        });


        clickView(R.id.btn_init_pay, view -> {
            if (checkOpen()) {
                int mul = getIntValue(etMul);
                short no = (short) getIntValue(etNo);
                addLog("Initiate collection, the channel number is " + no + ",and the collection amount is " + mul + " times the minimum payment unit.");
                IPReplyPara para = new IPReplyPara(no, mul);
                mBoard.initPayment(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_finish_pay, view -> {
            if (checkOpen()) {
                addLog("Payment received.");
                PayReplyPara para = new PayReplyPara(true);
                mBoard.notifyPayment(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Payment completed, end of this process.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_cancel_pay, view -> {
            if (checkOpen()) {
                addLog("Cancel Payment");
                PayReplyPara para = new PayReplyPara(false);
                mBoard.notifyPayment(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("The sales process has been cancelled.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_sellout_notify, view -> {
            if (checkOpen()) {
                RadioButton btn = findViewById(R.id.rtb_suc);
                boolean suc = btn.isChecked();
                addLog("Notification machine sales " + (suc ? "successful" : "failed"));
                ResultReplyPara para = new ResultReplyPara(suc);
                mBoard.notifyResult(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Complete the transaction and set the payment amount to zero.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_balance_change, view -> {
            if (checkOpen()) {
                int mul = getIntValue(etMulBalance);
                addLog("Initiate the change process.-" + mul);
                CBReplyPara para = new CBReplyPara(mul);
                mBoard.changeBalance(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_query_balance_change, view -> {
            if (checkOpen()) {
                addLog("Query for coin dispenser change result");
                BRReplyPara para = new BRReplyPara();
                mBoard.findChangeResult(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Change count：" + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_shipment, view -> {
            if (checkOpen()) {
                int addr = getIntValue(etAddr);
                int no = getIntValue(etWayNo);
                int type = getIntValue(etWayType);
                RadioButton rbtCheck = findViewById(R.id.rtb_check_open);
                boolean check = rbtCheck.isChecked();
                RadioButton rbtLift = findViewById(R.id.rtb_lift_open);
                boolean lift = rbtLift.isChecked();
                addLog("Vend from channel " + no);
                SReplyPara para = new SReplyPara(addr, no, type, check, lift);
                mBoard.Shipment(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_get_shipment_status, view -> {
            if (checkOpen()) {
                int addr = getIntValue(etAddr);
                addLog("Check the status of the driver board.");
                SSReplyPara para = new SSReplyPara(addr);
                mBoard.GetShipmentStatus(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("Running state is " + para.getRunStatus() + "-" + CodeUtil.getShipmentStatusMsg(para.getRunStatus()) + ",Fault code is " + para.getFaultCode() + "-" + CodeUtil.getFaultMsg(para.getFaultCode()));
                } else {
                    addError(para);
                }
            }
        });


        clickView(R.id.btn_read_lift_y, view -> {
            if (checkOpen()) {
                addLog("Read the set position information of the Y-axis.");
                int addr = getIntValue(etAddr);
                YPReplyPara para = new YPReplyPara(addr);
                mBoard.GetYPos(para);
                if (para.isOK()) {
                    addLog("Y-axis position is " + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_set_lift_y, view -> {
            if (checkOpen()) {
                addLog("Set the addressing position of the Y-axis lift motor.");
                int addr = getIntValue(etAddr);
                int v0 = getIntValue(findViewById(R.id.et_y0));
                int v1 = getIntValue(findViewById(R.id.et_y1));
                int v2 = getIntValue(findViewById(R.id.et_y2));
                int v3 = getIntValue(findViewById(R.id.et_y3));
                int v4 = getIntValue(findViewById(R.id.et_y4));
                int v5 = getIntValue(findViewById(R.id.et_y5));
                int v6 = getIntValue(findViewById(R.id.et_y6));
                int v7 = getIntValue(findViewById(R.id.et_y7));
                int v8 = getIntValue(findViewById(R.id.et_y8));
                int v9 = getIntValue(findViewById(R.id.et_y9));
                SYPReplyPara para = new SYPReplyPara(addr, v0, v1, v2, v3, v4, v5, v6, v7, v8, v9);
                mBoard.SeYPos(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_read_lift_x, view -> {
            if (checkOpen()) {
                addLog("Read the set position information of the X-axis.");
                int addr = getIntValue(etAddr);
                XPReplyPara para = new XPReplyPara(addr);
                mBoard.GetXPos(para);
                if (para.isOK()) {
                    addLog("X-axis position is " + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_set_lift_x, view -> {
            if (checkOpen()) {
                addLog("Set the horizontal motor X-axis addressing position.");
                int addr = getIntValue(etAddr);
                int v0 = getIntValue(findViewById(R.id.et_y0));
                int v1 = getIntValue(findViewById(R.id.et_y1));
                int v2 = getIntValue(findViewById(R.id.et_y2));
                int v3 = getIntValue(findViewById(R.id.et_y3));
                int v4 = getIntValue(findViewById(R.id.et_y4));
                int v5 = getIntValue(findViewById(R.id.et_y5));
                int v6 = getIntValue(findViewById(R.id.et_y6));
                int v7 = getIntValue(findViewById(R.id.et_y7));
                int v8 = getIntValue(findViewById(R.id.et_y8));
                int v9 = getIntValue(findViewById(R.id.et_y9));
                SXPReplyPara para = new SXPReplyPara(addr, v0, v1, v2, v3, v4, v5, v6, v7, v8, v9);
                mBoard.SeXPos(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_to_lift_y, view -> {
            if (checkOpen()) {
                addLog("Send Y-axis motor addressing command.");
                int addr = getIntValue(etAddr);
                int pos = getIntValue(findViewById(R.id.et_to_y));
                TYReplyPara para = new TYReplyPara(addr, (short) pos);
                mBoard.ToY(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_to_lift_x, view -> {
            if (checkOpen()) {
                addLog("Send X-axis motor addressing command.");
                int addr = getIntValue(etAddr);
                int pos = getIntValue(findViewById(R.id.et_to_x));
                TXReplyPara para = new TXReplyPara(addr, (short) pos);
                mBoard.ToX(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_pick_y, view -> {
            if (checkOpen()) {
                addLog("Set the Y-axis position of the pickup point.");
                int addr = getIntValue(etAddr);
                int pos = getIntValue(findViewById(R.id.et_pick_y));
                PYReplyPara para = new PYReplyPara(addr, (short) pos);
                mBoard.SetPickY(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_pick_x, view -> {
            if (checkOpen()) {
                addLog("Set the X-axis position of the pickup point.");
                int addr = getIntValue(etAddr);
                int pos = getIntValue(findViewById(R.id.et_pick_x));
                PXReplyPara para = new PXReplyPara(addr, (short) pos);
                mBoard.SetPickX(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_start, view -> {
            if (checkOpen()) {
                addLog("Control the output of the lifting board.");
                int addr = getIntValue(etAddr);
                int no = ((RadioButton) findViewById(R.id.rtb_lift_y)).isChecked() ? 0 : 1;
                int mode = ((RadioButton) findViewById(R.id.rtb_lift_mode0)).isChecked() ? 0 : 1;
                int status = ((RadioButton) findViewById(R.id.rtb_lift_status1)).isChecked() ? 1 : (((RadioButton) findViewById(R.id.rtb_lift_status2)).isChecked() ? 2 : 0);
                int time = getIntValue(findViewById(R.id.et_lift_time));
                int channel = getIntValue(findViewById(R.id.et_lift_channel));

                DCReplyPara para = new DCReplyPara(addr, no, mode, channel, status, time);
                mBoard.DriveOutput(para);
                if (para.isOK()) {
                    addLog("Command sent successfully.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_y_io, view -> {
            if (checkOpen()) {
                addLog("Read Y-axis motor input IO status.");
                int addr = getIntValue(etAddr);
                YioReplyPara para = new YioReplyPara(addr);
                mBoard.GetYIOStatus(para);
                if (para.isOK()) {
                    addLog("Status is " + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_x_io, view -> {
            if (checkOpen()) {
                addLog("Read X-axis motor input IO status.");
                int addr = getIntValue(etAddr);
                XioReplyPara para = new XioReplyPara(addr);
                mBoard.GetXIOStatus(para);
                if (para.isOK()) {
                    addLog("Status is " + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_io, view -> {
            if (checkOpen()) {
                addLog("Read the input IO status of the driver board.");
                int addr = getIntValue(etAddr);
                IOReplyPara para = new IOReplyPara(addr);
                mBoard.GetIOStatus(para);
                if (para.isOK()) {
                    addLog("Status is " + para.toString());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_y_, view -> {
            if (checkOpen()) {
                addLog("Read the status of the Y-axis lifting motor.¬");
                int addr = getIntValue(etAddr);
                YSReplyPara para = new YSReplyPara(addr);
                mBoard.GetYStatus(para);
                if (para.isOK()) {
                    addLog("Status is " + para.getRunStatus() + "-" + CodeUtil.getXYStatusMsg(para.getRunStatus()) + ",the failure code is " + para.getFaultCode() + "-" + CodeUtil.getFaultMsg(para.getFaultCode()));
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_lift_x_, view -> {
            if (checkOpen()) {
                addLog("Read X-axis motor status.");
                int addr = getIntValue(etAddr);
                XSReplyPara para = new XSReplyPara(addr);
                mBoard.GetXStatus(para);
                if (para.isOK()) {
                    addLog("Status is " + para.getRunStatus() + "-" + CodeUtil.getXYStatusMsg(para.getRunStatus()) + ",the failure code is " + para.getFaultCode() + "-" + CodeUtil.getFaultMsg(para.getFaultCode()));
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_hwc, view -> {
            if (checkOpen()) {
                addLog("Query driver board hardware version.");
                int addr = getIntValue(etAddr);
                cc.uling.usdk.board.wz.para.HCReplyPara para = new cc.uling.usdk.board.wz.para.HCReplyPara(addr);
                mBoard.ReadHardwareConfig(para);
                if (para.isOK()) {
                    addLog("Version is " + para.getVersion() + ", there are " + para.getRow() + " rows and " + para.getColumn() + " columns.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_swv, view -> {
            if (checkOpen()) {
                addLog("Check driver board software version.");
                int addr = getIntValue(etAddr);
                cc.uling.usdk.board.wz.para.SVReplyPara para = new cc.uling.usdk.board.wz.para.SVReplyPara(addr);
                mBoard.GetSoftwareVersion(para);
                if (para.isOK()) {
                    addLog("version:" + para.getVersion());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_read_temp, view -> {
            if (checkOpen()) {
                addLog("Read temperature and humidity in the device.");
                int addr = getIntValue(etAddr);
                TempReplyPara para = new TempReplyPara(addr);
                mBoard.ReadTemp(para);
                Log.i(TAG, para.toString());
                if (para.isOK()) {
                    addLog("The temperature is " + para.getTemp() / 10.0 + ",and the humidity is " + para.getHumi() / 10.0);
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_gzg_status, view -> {
            if (checkOpen()) {
                addLog("Check the current status of the grid cabinet.");
                int addr = getIntValue(etAddr);
                int no = getIntValue(findViewById(R.id.et_box_no));
                BSReplyPara para = new BSReplyPara(addr, no);
                mBoard.GetBoxStatus(para);
                if (para.isOK()) {
                    addLog("Vending lane number " + para.getNo() + " " + (para.getStatus() == 0 ? "opened." : "closed."));
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_drop_status, view -> {
            if (checkOpen()) {
                addLog("Check the shipping status of the device.");
                int addr = getIntValue(etAddr);
                DSReplyPara para = new DSReplyPara(addr);
                mBoard.GetDropStatus(para);
                if (para.isOK()) {
                    addLog("Shipping status is " + para.getStatus() + "," + (para.getStatus() == 0 ? "Detection of disconnection or obstruction." : "Normal uncensored"));
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_left_reset, view -> {
            if (checkOpen()) {
                addLog("Send the reset command for the lift motor.");
                int addr = getIntValue(etAddr);
                ResetReplyPara para = new ResetReplyPara(addr);
                mBoard.ResetLift(para);
                if (para.isOK()) {
                    addLog("Reset successful.");
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_get_pos_y, view -> {
            if (checkOpen()) {
                addLog("Query the current position of the lift motor.");
                int addr = getIntValue(etAddr);
                CYReplyPara para = new CYReplyPara(addr);
                mBoard.GetYPos(para);
                if (para.isOK()) {
                    addLog("The current position is " + para.getValue());
                } else {
                    addError(para);
                }
            }
        });

        clickView(R.id.btn_get_pos_x, new ActionRunnable<CXReplyPara>() {
            @Override
            public CXReplyPara getValue() {
                addLog("Query the current position of the water level motor.");
                CXReplyPara para = new CXReplyPara(getIntValue(etAddr));
                mBoard.GetXPos(para);
                return para;
            }

            @Override
            public void todo(CXReplyPara para) {
                addLog("The current position is " + para.getValue());
            }
        });


        clickView(R.id.btn_array_status, new ActionRunnable<ASReplyPara>() {
            @Override
            public ASReplyPara getValue() {
                addLog("Check the output status of the driver board array.");
                ASReplyPara para = new ASReplyPara(getIntValue(etAddr));
                mBoard.GetArrayStatus(para);
                return para;
            }

            @Override
            public void todo(ASReplyPara para) {
                addLog("The array output status is " + para.toString());
            }
        });

        clickView(R.id.btn_lauch_moto, new ActionRunnable<RMReplyPara>() {
            @Override
            public RMReplyPara getValue() {
                addLog("Manually set the motor to run.");
                short mode = (short) (((RadioButton) findViewById(R.id.rtb_moto_type1)).isChecked() ? 1 : 0);
                short status = (short) (((RadioButton) findViewById(R.id.rtb_moto_status1)).isChecked() ? 1 : (((RadioButton) findViewById(R.id.rtb_moto_status2)).isChecked() ? 2 : 0));
                RMReplyPara para = new RMReplyPara(getIntValue(etAddr), mode, status);
                mBoard.RunMoto(para);
                return para;
            }

            @Override
            public void todo(RMReplyPara para) {
                addLog("Command sent successfully.");
            }
        });

        clickView(R.id.btn_moto_time, new ActionRunnable<MTReplyPara>() {
            @Override
            public MTReplyPara getValue() {
                addLog("Set motor run timeout.");
                int time = getIntValue(findViewById(R.id.et_moto_time));
                MTReplyPara para = new MTReplyPara(getIntValue(etAddr), (short) time);
                mBoard.MotoTimeout(para);
                return para;
            }

            @Override
            public void todo(MTReplyPara para) {
                addLog("Command sent successfully.");
            }
        });

        clickView(R.id.btn_array_output, new ActionRunnable<AOReplyPara>() {
            @Override
            public AOReplyPara getValue() {
                addLog("Control the output of the driver board array.");
                int[] array = new int[20];
                array[0] = getIntValue(findViewById(R.id.et_n0));
                array[1] = getIntValue(findViewById(R.id.et_n1));
                array[2] = getIntValue(findViewById(R.id.et_n2));
                array[3] = getIntValue(findViewById(R.id.et_n3));
                array[4] = getIntValue(findViewById(R.id.et_n4));
                array[5] = getIntValue(findViewById(R.id.et_n5));
                array[6] = getIntValue(findViewById(R.id.et_n6));
                array[7] = getIntValue(findViewById(R.id.et_n7));
                array[8] = getIntValue(findViewById(R.id.et_n8));
                array[9] = getIntValue(findViewById(R.id.et_n9));

                array[10] = getIntValue(findViewById(R.id.et_p0));
                array[11] = getIntValue(findViewById(R.id.et_p1));
                array[12] = getIntValue(findViewById(R.id.et_p2));
                array[13] = getIntValue(findViewById(R.id.et_p3));
                array[14] = getIntValue(findViewById(R.id.et_p4));
                array[15] = getIntValue(findViewById(R.id.et_p5));
                array[16] = getIntValue(findViewById(R.id.et_p6));
                array[17] = getIntValue(findViewById(R.id.et_p7));
                array[18] = getIntValue(findViewById(R.id.et_p8));
                array[19] = getIntValue(findViewById(R.id.et_p9));

                AOReplyPara para = new AOReplyPara(getIntValue(etAddr), getIntValue(findViewById(R.id.et_ef_time)), array);
                mBoard.ArrayOutput(para);
                return para;
            }

            @Override
            public void todo(AOReplyPara para) {
                addLog("Command sent successfully.");
            }
        });


        clickView(R.id.btn_data, view -> {
            initLogFilesView("scd");
        });
        clickView(R.id.btn_read_log, view -> {
            initLogFilesView("crash");
        });

        clickView(R.id.btn_close_files, view -> findViewById(R.id.ll_files).setVisibility(View.GONE));
        clickView(R.id.btn_close_detail, view -> findViewById(R.id.ll_log_detail).setVisibility(View.GONE));

    }

    private void clickView(int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    private <T extends BaseClsPara> void clickView(int id, ActionRunnable<T> runnable) {
        clickView(id, view -> {
            if (checkOpen()) {
                try {
                    T para = runnable.getValue();
                    if (para.isOK()) {
                        runnable.todo(para);
                    } else {
                        addError(para);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    addLog("Exception|" + e.getMessage());
                }
            }
        });
    }

    private void initLogFilesView(String tag) {
        loadFiles(tag, findViewById(R.id.lv_files));
    }

    private void iniListViewAdapter(ListView lv, ArrayList<String> strings) {
        findViewById(R.id.ll_files).setVisibility(View.VISIBLE);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            String file = strings.get(i);
            loadFileLogInfo(file);
        });
    }

    private void iniLogListViewAdapter(ArrayList<String> strings) {
        findViewById(R.id.ll_log_detail).setVisibility(View.VISIBLE);
        ListView lv = findViewById(R.id.lv_log_detail);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings);
        lv.setAdapter(adapter);
    }

    private void loadFiles(String tag, ListView lv) {
        TPoolManager.getInstance().execute(() -> {
            final ArrayList<String> files = new ArrayList<>();
            File dirPath = getApplication().getExternalFilesDir(null);
            File file = new File(dirPath.getAbsolutePath() + File.separator + "logs" + (null == tag ? "" : "/" + tag));
            if (file.exists()) {
                File[] subFiles = file.listFiles();
                for (int i = 0; i < subFiles.length; i++) {
                    if (subFiles[i].isFile()) {
                        files.add(subFiles[i].getAbsolutePath());
                    }
                }
            }
            mHandler.post(() -> {
                if (!isFinishing()) {
                    iniListViewAdapter(lv, files);
                }
            });
        });
    }

    private void loadFileLogInfo(String filePath) {
        showLoadDialog();
        TPoolManager.getInstance().execute(() -> {
            final ArrayList<String> strings = new ArrayList<>();
            String logInfo = LogManager.readFromFile(filePath);
            String[] array = logInfo.split("\n");
            List<String> list = Arrays.asList(array);
            Collections.reverse(list);
            for (int i = 0; i < list.size(); i++) {
                if (i < 10000) {
                    strings.add(list.get(i));
                }
            }

            mHandler.post(() -> {
                if (!isFinishing()) {
                    hideLoadingDialog();
                    TextView name = findViewById(R.id.tv_file_name);
                    name.setText(filePath);
                    iniLogListViewAdapter(strings);
                }
            });
        });
    }

    private ProgressDialog mProgressDialog;

    private void showLoadDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading, please wait~");
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    private void hideLoadingDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    private void openCom() {
        String string = etCommid.getText().toString();
        if (!TextUtils.isEmpty(string)) {
            mBoard = USDK.getInstance().create(string);
            int ret = mBoard.EF_OpenDev(string, 9600);
            addLog(ret == ErrorConst.MDB_ERR_NO_ERR ? "Successfully opened" : "Failed to open" + " serial port " + string);
        } else {
            addLog("The serial port name cannot be empty.");
        }
    }

    private boolean isOpened() {
        return null != mBoard && mBoard.EF_Opened();
    }

    private boolean checkOpen() {
        if (!isOpened()) {
            addLog("Serial port is not open.");
            return false;
        }
        return true;
    }

    private void initView() {
        etNo = findViewById(R.id.et_no);
        etMul = findViewById(R.id.et_mul);
        etMulBalance = findViewById(R.id.et_mul_balance);

        etAddr = findViewById(R.id.et_board_addr);
        etWayNo = findViewById(R.id.et_way_no);
        etWayType = findViewById(R.id.et_way_type);
    }

    private void initLogView() {
        listView = findViewById(R.id.lv_logs);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLogs);
        listView.setAdapter(mAdapter);
    }

    private void addLog(String msg) {
        mLogs.add(0, msg);
        mAdapter.notifyDataSetChanged();
        listView.scrollTo(0, 0);
    }

    private void addError(BaseClsPara para) {
        addLog("Error code is " + para.getResultCode() + "-" + para.getErrorMsg());
    }

    private int getIntValue(EditText editText) {
        String string = editText.getText().toString();
        try {
            if (!TextUtils.isEmpty(string)) {
                return Integer.parseInt(string);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }


}