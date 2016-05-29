package com.nwa.smartgym.api;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.nwa.smartgym.activities.Devices;
import com.nwa.smartgym.api.callbacks.Callback;
import com.nwa.smartgym.lib.SecretsHelper;
import com.nwa.smartgym.models.Device;
import com.nwa.smartgym.models.HTTPResponse;

import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by robin on 29-5-16.
 */
public class DeviceAPIInterface {
    private Context context;
    private DeviceAPI deviceService;
    private Dao<Device, Integer> deviceDao;

    public DeviceAPIInterface(Context context, Dao<Device, Integer> dao) {
        this.context = context;
        this.deviceDao = dao;

        SecretsHelper secretsHelper = new SecretsHelper(context);
        this.deviceService = ServiceGenerator.createSmartGymService(DeviceAPI.class,
                secretsHelper.getAuthToken());
    }

    public void list() {
        Call<List<Device>> call = deviceService.listDevices();
        call.enqueue(new Callback<List<Device>>(context) {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.code() == 200) {
                    for (Device device : response.body()) {
                        try {
                            deviceDao.create(device);
                        } catch (SQLException e) {
                            Log.e(context.getClass().getName(), "Unable to persist device", e);
                        }
                    }
                } else {
                    raiseGenericError();
                }
            }
        });
    }

    public void delete(final Device device) {
        Call<HTTPResponse> call = deviceService.deleteDevice(device.getId());
        call.enqueue(new Callback<HTTPResponse>(context) {

            @Override
            public void onResponse(Call<HTTPResponse> call, Response<HTTPResponse> response) {
                System.out.println(response.code());
                if (response.code() == 200) {
                    try {
                        deviceDao.delete(device);
                    } catch (SQLException e) {
                        Log.e(context.getClass().getName(), "Unable to delete device", e);
                    }
                } else {
                    raiseGenericError();
                }
            }
        });
    }
}