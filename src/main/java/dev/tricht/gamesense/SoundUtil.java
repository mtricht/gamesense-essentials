package dev.tricht.gamesense;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.PointerByReference;

import static com.sun.jna.platform.win32.WTypes.CLSCTX_INPROC_SERVER;

/**
 * Source: https://github.com/serezhka/clickerbot/blob/master/src/main/java/com/github/serezhka/clickerbot/jna/SoundUtil.java
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class SoundUtil {

    // Empirical research
    private static final Guid.CLSID CLSID_MMDeviceEnumerator = new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");
    private static final Guid.IID IID_IMMDeviceEnumerator = new Guid.IID("A95664D2-9614-4F35-A746-DE8DB63617E6");
    private static final Guid.IID IID_IAudioEndpointVolume = new Guid.IID("5CDF2C82-841E-4546-9722-0CF74078229A");
    private static final Guid.IID IID_IAudioMeterInformation = new Guid.IID("C02216F6-8C67-4B5B-9D00-D008E73E0064");

    private static final PointerByReference MMDeviceEnumerator = new PointerByReference();
    private static final PointerByReference MMDevice = new PointerByReference();
    private static final PointerByReference AudioEndpointVolume = new PointerByReference();
    private static final PointerByReference AudioMeterInformation = new PointerByReference();

    static {

        if (!WinNT.S_OK.equals(Ole32.INSTANCE.CoInitialize(null))) {
            throw new RuntimeException("Ole32::CoInitialize() failed");
        }

        if (!WinNT.S_OK.equals(Ole32.INSTANCE.CoCreateInstance(
                CLSID_MMDeviceEnumerator,
                null,
                CLSCTX_INPROC_SERVER,
                IID_IMMDeviceEnumerator,
                MMDeviceEnumerator))) {
            throw new RuntimeException("Ole32::CoCreateInstance() failed");
        }

        Pointer MMDeviceEnumeratorPointer = MMDeviceEnumerator.getValue();
        Pointer MMDeviceEnumeratorVirtualTable = MMDeviceEnumeratorPointer.getPointer(0);

        // Empirical research
        int GetDefaultAudioEndpointOffset = 4 * WinDef.DWORDLONG.SIZE;
        Function GetDefaultAudioEndpoint = Function.getFunction(MMDeviceEnumeratorVirtualTable.getPointer(GetDefaultAudioEndpointOffset), Function.ALT_CONVENTION);

        // Empirical research
        int eDataFlow = 0; // eRender
        int eRole = 1; // eMultimedia
        if (!WinNT.S_OK.equals(GetDefaultAudioEndpoint.invoke(WinNT.HRESULT.class, new Object[]{MMDeviceEnumeratorPointer, eDataFlow, eRole, MMDevice}))) {
            throw new RuntimeException("IMMDeviceEnumerator::GetDefaultAudioEndpoint() failed");
        }

        Pointer MMDevicePointer = MMDevice.getValue();
        Pointer MMDeviceVirtualTable = MMDevicePointer.getPointer(0);

        // Empirical research
        int ActivateOffset = 3 * WinDef.DWORDLONG.SIZE;
        Function Activate = Function.getFunction(MMDeviceVirtualTable.getPointer(ActivateOffset), Function.ALT_CONVENTION);

        if (!WinNT.S_OK.equals(Activate.invoke(WinNT.HRESULT.class, new Object[]{MMDevicePointer, IID_IAudioEndpointVolume, CLSCTX_INPROC_SERVER, null, AudioEndpointVolume}))) {
            throw new RuntimeException("IMMDevice::Activate( AudioEndpointVolume ) failed");
        }

        if (!WinNT.S_OK.equals(Activate.invoke(WinNT.HRESULT.class, new Object[]{MMDevicePointer, IID_IAudioMeterInformation, CLSCTX_INPROC_SERVER, null, AudioMeterInformation}))) {
            throw new RuntimeException("IMMDevice::Activate( AudioMeterInformation ) failed");
        }
    }

    private static final Function GetMasterVolumeLevel = GetMasterVolumeLevelFunction();
    private static final Function GetPeakValue = GetPeakValueFunction();

    private static Function GetMasterVolumeLevelFunction() {
        Pointer AudioEndpointVolumePointer = AudioEndpointVolume.getValue();
        Pointer AudioEndpointVolumeVirtualTable = AudioEndpointVolumePointer.getPointer(0);
        // Empirical research
        int GetMasterVolumeLevelFunctionOffset = 9 * WinDef.DWORDLONG.SIZE;
        return Function.getFunction(AudioEndpointVolumeVirtualTable.getPointer(GetMasterVolumeLevelFunctionOffset), Function.ALT_CONVENTION);
    }

    private static Function GetPeakValueFunction() {
        Pointer AudioMeterInformationPointer = AudioMeterInformation.getValue();
        Pointer AudioMeterInformationVirtualTable = AudioMeterInformationPointer.getPointer(0);
        // Empirical research
        int GetPeakValueFunctionOffset = 3 * WinDef.DWORDLONG.SIZE;
        return Function.getFunction(AudioMeterInformationVirtualTable.getPointer(GetPeakValueFunctionOffset), Function.ALT_CONVENTION);
    }

    /**
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/dd370930">msdn</a>
     */
    public static float getMasterVolumeLevel() {
        FloatByReference resultPointer = new FloatByReference();
        if (!WinNT.S_OK.equals(GetMasterVolumeLevel.invoke(WinNT.HRESULT.class, new Object[]{AudioEndpointVolume.getValue(), resultPointer}))) {
            throw new RuntimeException("IAudioEndpointVolume::GetMasterVolumeLevel() failed");
        }
        return Math.round(resultPointer.getValue() * 100.0) / 100.0f;
    }

    /**
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/dd368230">msdn</a>
     */
    public static float getPeakValue() {
        FloatByReference resultPointer = new FloatByReference();
        if (!WinNT.S_OK.equals(GetPeakValue.invoke(WinNT.HRESULT.class, new Object[]{AudioMeterInformation.getValue(), resultPointer}))) {
            throw new RuntimeException("IAudioMeterInformation::GetPeakValue() failed");
        }
        return Math.round(resultPointer.getValue() * 100.0) / 100.0f;
    }
}