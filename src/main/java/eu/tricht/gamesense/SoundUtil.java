package eu.tricht.gamesense;

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
 *
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class SoundUtil {

    // Empirical research
    private static final Guid.CLSID CLSID_MMDeviceEnumerator = new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");
    private static final Guid.IID IID_IMMDeviceEnumerator = new Guid.IID("A95664D2-9614-4F35-A746-DE8DB63617E6");
    private static final Guid.IID IID_IAudioEndpointVolume = new Guid.IID("5CDF2C82-841E-4546-9722-0CF74078229A");

    private static final PointerByReference MMDeviceEnumerator = new PointerByReference();
    private static final PointerByReference MMDevice = new PointerByReference();
    private static final PointerByReference AudioEndpointVolume = new PointerByReference();

    private static Function GetMasterVolumeLevel;

    private static boolean initialized = false;

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
    }

    private static void Initialize() {
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
        GetMasterVolumeLevel = GetMasterVolumeLevelFunction();
        initialized = true;
    }

    private static Function GetMasterVolumeLevelFunction() {
        Pointer AudioEndpointVolumePointer = AudioEndpointVolume.getValue();
        Pointer AudioEndpointVolumeVirtualTable = AudioEndpointVolumePointer.getPointer(0);
        // Empirical research
        int GetMasterVolumeLevelFunctionOffset = 9 * WinDef.DWORDLONG.SIZE;
        return Function.getFunction(AudioEndpointVolumeVirtualTable.getPointer(GetMasterVolumeLevelFunctionOffset), Function.ALT_CONVENTION);
    }

    /**
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/dd370930">msdn</a>
     */
    public static float getMasterVolumeLevel() {
        try {
            if (!initialized) {
                Initialize();
            }
            FloatByReference resultPointer = new FloatByReference();
            if (!WinNT.S_OK.equals(GetMasterVolumeLevel.invoke(WinNT.HRESULT.class, new Object[]{AudioEndpointVolume.getValue(), resultPointer}))) {
                throw new RuntimeException("IAudioEndpointVolume::GetMasterVolumeLevel() failed");
            }
            return Math.round(resultPointer.getValue() * 100.0) / 100.0f;
        } catch (Exception e) {
            initialized = false;
            return 0;
        }
    }
}