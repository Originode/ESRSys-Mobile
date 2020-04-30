package ph.esrconstruction.esrsys.esrsysmobile.events;

public class TimerEvent extends MessageEvent {
    public final String message;
    public final long startTime;
    public final long duration;

    public TimerEvent(String message, long startTime, long duration_ms) {
        super(message);
        this.message = message;
        this.startTime = startTime;
        this.duration = duration_ms;
    }

    public final class Messages {
        public static final String EmployeeSync = "EmployeeSync";
    }
}
