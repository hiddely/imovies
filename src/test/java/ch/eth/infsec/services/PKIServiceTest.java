package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import ch.eth.infsec.services.pki.PKIService;
import ch.eth.infsec.util.CAUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PKIServiceTest {

    File cryptoFolder = new File(CAUtil.cryptoPath);
    User user = new User();

    @Autowired
    PKIService pkiService;

    @Before
    public void setUp() throws Exception {
        //deleteFolder(cryptoPath);

        user.setUid("ABC");
        user.setEmail("hello@gmail.com");
        user.setPwd("pwd");
        user.setFirstname("John");
        user.setLastname("Appleseed");
    }

    @Test
    public void testIssueCertificate() {
        String result = pkiService.issueCertificate(user, "password", null);

        assertNotNull(result);
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
