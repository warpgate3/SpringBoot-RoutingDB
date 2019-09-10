package info.m2sj.springrouterdatasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@SpringBootApplication
public class SpringrouterdatasourceApplication {
    private MyMapper myMapper;

    public SpringrouterdatasourceApplication(MyMapper myMapper) {
        this.myMapper = myMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringrouterdatasourceApplication.class, args);
    }

    @RestController
    @RequestMapping("/route")
    class TestController {

        @GetMapping("/a")
        public String getA(HttpSession session) {
            session.setAttribute("db_key","db01");
            return myMapper.findDbName();
        }

        @GetMapping("/b")
        public String getB(HttpSession session) {
            session.setAttribute("db_key","db02");
            return myMapper.findDbName();
        }
    }
}
