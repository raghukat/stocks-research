import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.helper.IonHelper;
import com.stocks.research.mkvapi_dummy.main.DummyFileBasedSubscriber;
import com.stocks.research.mkvapi_dummy.main.TestFileSubscriber;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMessageTest {


    final static String TEST_FOLDER = "src/test/resources/integrationtestdata";

    @BeforeClass
    public static void onceExecutedBeforeAll() {

        System.setProperty(Ion.FACTORY_PROPERTY, Ion.DUMMY_FACTORY);
        Ion.reset();
        DummyFileBasedSubscriber.setDataDirectory(TEST_FOLDER);
        IonHelper.startMkvAndAwaitReady();
        TestFileSubscriber testFileSubscriber = new TestFileSubscriber();

    }

    @Test
    public void testMessage() throws Exception {
//        TestFileSubscriber testFileSubscriber = new TestFileSubscriber();
        Thread.sleep(1000 * 600);

    }
}
