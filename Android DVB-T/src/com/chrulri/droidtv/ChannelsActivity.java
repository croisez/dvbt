package com.chrulri.droidtv;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import app.goprasoft.dvb.tv.R;

import com.chrulri.droidtv.utils.ErrorUtils;
import com.chrulri.droidtv.utils.PreferenceUtils;
import com.chrulri.droidtv.utils.ProcessUtils;
import com.chrulri.droidtv.utils.StringUtils;
import com.chrulri.droidtv.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChannelsActivity extends Activity {
    private static final String TAG = ChannelsActivity.class.getSimpleName();

    private Spinner mSpinner;
    private ListView mListView;
    private String[] mChannelConfigs;
    private String[] mChannels;

    // ************************************************************** //
    // * ACTIVITY *************************************************** //
    // ************************************************************** //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">>> onCreate");
        super.onCreate(savedInstanceState);
     
        Log.d(TAG, "<<< onCreate");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, ">>> onDestroy");
        super.onDestroy();
        Log.d(TAG, "<<< onDestroy");
    }

    private void onCreateImpl() {
        setContentView(R.layout.channels);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                loadChannels((String) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadChannels(null);
            }
        });

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                watchChannel((int) id);
            }
        });

        // start
        loadChannelLists();
        setChannelList(null);
        if (mSpinner.getAdapter().getCount() == 0) {
            PreferenceUtils.openSettings(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            PreferenceUtils.openSettings(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ************************************************************** //
    // * CHANNEL UTILS ********************************************** //
    // ************************************************************** //

    private void loadChannelLists() {
        // reset channel lists spinner
        mSpinner.setAdapter(null);
        mListView.setAdapter(null);

        // load channel lists
        String[] configFiles = Utils.getConfigsDir(this).list();
        mSpinner.setAdapter(Utils.createSimpleArrayAdapter(this, configFiles));
    }

    private void loadChannels(String channelListName) {
        // reset channel list view
        mListView.setAdapter(null);

        // check channel list
        File file = Utils.getConfigsFile(this, channelListName);
        if (file == null || !file.canRead() || !file.isFile()) {
            return;
        }

        // load channels
        mChannelConfigs = null;
        mChannels = null;
        try {
            List<String> channelConfigList = new ArrayList<String>();
            List<String> channelList = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                channelConfigList.add(line);
                channelList.add(line.split(":")[0]);
            }
            reader.close();
            mChannelConfigs = channelConfigList.toArray(new String[channelConfigList
                    .size()]);
            mChannels = channelList.toArray(new String[channelList.size()]);
        } catch (IOException e) {
            ErrorUtils.error(this, getText(R.string.error_invalid_channel_configuration), e);
        }

        // update channel list view
        mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
                mChannels));
    }

    /**
     * @param channelList list name, insert null to read config from settings
     */
    private void setChannelList(String channelList) {
        if (channelList == null) {
            channelList = PreferenceUtils.get(this).getString(PreferenceUtils.KEY_CHANNELCONFIGS, null);
        }

        if (StringUtils.isNullOrEmpty(channelList) || Utils.getConfigsFile(this,
                channelList) == null) {
            String[] files = Utils.getConfigsDir(this).list();
            if (files.length == 1) {
                channelList = files[0];
            } else {
                channelList = null;
            }
        }

        if (channelList == null) {
            mSpinner.setSelection(AdapterView.INVALID_POSITION);
        } else {
            for (int i = 0; i < mSpinner.getAdapter().getCount(); i++) {
                if (channelList.equals(mSpinner.getAdapter().getItem(i))) {
                    mSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void watchChannel(int channelId) {
        Log.d(TAG, "watchChannel(" + channelId + "): " + mChannels[channelId]);
        String channelconfig = mChannelConfigs[channelId];
        Intent intent = new Intent(this, StreamActivity.class);
        intent.putExtra(StreamActivity.EXTRA_CHANNELCONFIG, channelconfig);
        startActivity(intent);
    }

    // ************************************************************** //
    // * INITIALIZATION TASK **************************************** //
    // ************************************************************** //

    

        static final int OK = 0;
        static final int CHECK_DEVICE = R.string.no_device;

        static final String LEGACY_DEV_DVB_ADAPTER = "/dev/dvb0";
        final File LEGACY_FILE_DVB_CA = new File(LEGACY_DEV_DVB_ADAPTER + ".ca0");
        final File LEGACY_FILE_DVB_FRONTEND = new File(LEGACY_DEV_DVB_ADAPTER + ".frontend0");
        final File LEGACY_FILE_DVB_DEMUX = new File(LEGACY_DEV_DVB_ADAPTER + ".demux0");
        final File LEGACY_FILE_DVB_DVR = new File(LEGACY_DEV_DVB_ADAPTER + ".dvr0");
        

        static final String DEV_DVB_ADAPTER = "/dev/dvb/adapter0";
        final File FILE_DVB_CA = new File(DEV_DVB_ADAPTER + "/ca0");
        final File FILE_DVB_FRONTEND = new File(DEV_DVB_ADAPTER + "/frontend0");
        final File FILE_DVB_DEMUX = new File(DEV_DVB_ADAPTER + "/demux0");
        final File FILE_DVB_DVR = new File(DEV_DVB_ADAPTER + "/dvr0");

        private boolean verifyDeviceNode(File deviceNode, File legacyDeviceNode, boolean checkExists) {
            if (checkExists) {
                if (deviceNode.exists()) {
                    if (!deviceNode.canRead() || !deviceNode.canWrite()) {
                        fixPermission(deviceNode);
                    }
                } else if (legacyDeviceNode.exists()) {
                    if (!legacyDeviceNode.canRead() || !legacyDeviceNode.canWrite()) {
                        fixPermission(legacyDeviceNode);
                    }
                    createSymlink(legacyDeviceNode, deviceNode);
                } else {
                    return false;
                }
                return deviceNode.exists() && deviceNode.canRead() && deviceNode.canWrite();
            } else {
                if (deviceNode.exists() || legacyDeviceNode.exists()) {
                    return verifyDeviceNode(deviceNode, legacyDeviceNode, true);
                } else {
                    return true;
                }
            }
        }

        private void fixPermission(File file) {
            try {
                Process p = ProcessUtils.runAsRoot("chmod", "666", file.getAbsolutePath());
                p.waitFor();
            } catch (Exception e) {
                Log.w(TAG, "Failed to fix permission: " + file.getAbsolutePath(), e);
            }
        }

        private void createSymlink(File target, File symlink) {
            try {
                Process p = ProcessUtils.runAsRoot(
                        "mkdir", "-p", symlink.getParentFile().getAbsolutePath(), "\n",
                        "ln", "-s", target.getAbsolutePath(), symlink.getAbsolutePath());
                p.waitFor();
            } catch (Exception e) {
                Log.w(TAG, "Failed to create symlink: " + target.getAbsolutePath() + " <- "
                        + symlink.getAbsolutePath());
            }
        }

        private boolean checkDevice() {
            return verifyDeviceNode(FILE_DVB_FRONTEND, LEGACY_FILE_DVB_FRONTEND, true)
                    && verifyDeviceNode(FILE_DVB_DEMUX, LEGACY_FILE_DVB_DEMUX, true)
                    && verifyDeviceNode(FILE_DVB_DVR, LEGACY_FILE_DVB_DVR, true)
                    && verifyDeviceNode(FILE_DVB_CA, LEGACY_FILE_DVB_CA, false);
        }


}
