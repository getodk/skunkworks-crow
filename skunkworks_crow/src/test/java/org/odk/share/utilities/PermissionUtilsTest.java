package org.odk.share.utilities;

import android.Manifest;
import android.app.Application;
import android.location.LocationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLocationManager;

import static android.content.Context.LOCATION_SERVICE;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PermissionUtilsTest {
    private ShadowApplication shadowApplication;
    private Application application;

    @Before
    public void setUp() throws Exception {
        application = RuntimeEnvironment.application;
        shadowApplication = Shadows.shadowOf(application);
    }

    /**
     * {@link Test} for the location permissions and GPS.
     */
    @Test
    public void locationPermissionTest() {
        shadowApplication.grantPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);

        LocationManager locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = Shadows.shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);

        assertTrue(PermissionUtils.isGPSEnabled(application.getApplicationContext()));
    }
}
