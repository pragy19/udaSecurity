package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.image.ImageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private SecurityService securityService;

    // Requirement 1: If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    void armedSystemAndSensorActivated_setsPendingAlarm() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(repository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // Requirement 2: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set off the alarm.
    @Test
    void pendingAlarmAndSensorActivated_setsAlarm() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 3: If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    void pendingAlarmAndSensorsInactive_setsNoAlarm() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Back Door", SensorType.DOOR);
        sensor.setActive(true); // Sensor starts as active

        securityService.changeSensorActivationStatus(sensor, false); // Sensor becomes inactive

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 4: If alarm is active, change in sensor state should not affect the alarm state.
    @Test
    void activeAlarm_ignoresSensorStateChanges() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Sensor sensor = new Sensor("Garage", SensorType.DOOR);
        sensor.setActive(false);

        securityService.changeSensorActivationStatus(sensor, true);

        // We verify that setAlarmStatus was NEVER called with any status
        verify(repository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    // Requirement 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    @Test
    void sensorActivatedWhileAlreadyActiveAndPending_setsAlarm() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);
        sensor.setActive(true); // Sensor is already active

        securityService.changeSensorActivationStatus(sensor, true); // Activating again

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @Test
    void sensorDeactivatedWhileAlreadyInactive_makesNoChanges() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensor.setActive(false); // Sensor is already inactive

        securityService.changeSensorActivationStatus(sensor, false); // Deactivating again

        verify(repository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    // Requirement 7: If the camera image contains a cat while the system is armed-home, put the system into alarm status.
    @Test
    void catDetectedWhileArmedHome_setsAlarm() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(mock(BufferedImage.class));

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 8: If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @Test
    void noCatAndNoActiveSensors_setsNoAlarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(false); // Ensure sensor is inactive
        when(repository.getSensors()).thenReturn(Set.of(sensor));
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 9: If the system is disarmed, set the status to no alarm.
    @Test
    void systemDisarmed_setsNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 10: If the system is armed, reset all sensors to inactive.
    // Using a ParameterizedTest allows us to test both ARMED_HOME and ARMED_AWAY with one method!
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void systemArmed_resetsSensorsToInactive(ArmingStatus armingStatus) {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);
        when(repository.getSensors()).thenReturn(Set.of(sensor));

        securityService.setArmingStatus(armingStatus);

        assertFalse(sensor.getActive());
    }

    // Requirement 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @Test
    void armedHomeWhileCatDetected_setsAlarm() {
        // Setup: Assume the camera just saw a cat
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));

        // Action: User arms the system to Home
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // Verification
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }
}