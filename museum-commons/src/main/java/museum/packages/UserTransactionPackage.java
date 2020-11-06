package museum.packages;

import lombok.*;
import museum.donate.DonateType;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserTransactionPackage extends MuseumPackage {

	// request
	private UUID user;
	private DonateType donate;

	// response
	private String response;

//		INTERNAL_ERROR("Невозможно приобрести в данный момент. Попробуйте чуть позже", false),
//		INSUFFICIENT_FUNDS("Недостаточно средств на счёте", false),
//		ALREADY_BUYED("У вас уже имеется этот донат", false),
//		OK("Успешная покупка! Спасибо за поддержку разработчиков режима :з", true),

}
