package luolong.h2c;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>h2c客户端</p>
 *
 * @author luolong
 * @date 2018/10/10
 */
@RestController
@RequestMapping("/test")
public class H2CClient {

    @RequestMapping(value = "test" ,method = {RequestMethod.GET,RequestMethod.POST})
    String test() {
        return "test success";
    }

}
