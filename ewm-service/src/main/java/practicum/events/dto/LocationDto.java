package practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LocationDto {
  @Max(90)
  @Min(-90)
  @NotNull
  private Float lat;

  @Max(180)
  @Min(-180)
  @NotNull
  private Float lon;
}
