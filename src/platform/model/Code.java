package platform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(name = "code")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Code {
    @Id
    private UUID uuid;
    @Column(name = "date")
    private String date;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "max_views", nullable = false)
    private Integer maxView;
    @Column(name = "current_views", nullable = false)
    private Integer views;
    @Column(name = "max_time", nullable = false)
    private Integer time;

    public Code(String code, int maxView, int time) {
        uuid = UUID.randomUUID();
        this.date = format(LocalDateTime.now());
        this.code = code;
        this.maxView = maxView;
        this.time = time;
        this.views = 0;
    }

    public CodeRequest asRequest() {
        if (isSecret()) {
            if ((maxView > 0 && viewsLeft() > 0) || (time > 0 && timeLeft() > 0))
                return new CodeRequest(date, code, viewsLeft(), timeLeft());
            else
                return new CodeRequest(date, "Secret code", viewsLeft(), timeLeft());
        } else {
            return new CodeRequest(date, code, 0, 0);
        }
    }

    public boolean isSecret() {
        return (maxView > 0) || (time > 0);
    }

    public int viewsLeft() {
        return maxView > 0 ? getMaxView() - getViews() - 1 : 0;
    }

    public int timeLeft() {
        if (time > 0)
            return (int) ChronoUnit.SECONDS.between(LocalDateTime.now(), reformat().plusSeconds(time));
        return 0;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    public LocalDateTime reformat() {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }


}
