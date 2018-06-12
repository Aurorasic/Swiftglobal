import com.higgsblock.global.browser.dao.entity.MinerPO;
import com.higgsblock.global.browser.service.iface.IMinersService;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
public class MinersDaoTest extends BaseTest{

    @Autowired
    private IMinersService iMinersService;
    @Test
    public void add() {
        MinerPO miners = new MinerPO();
        miners.setAddress("address");
        Money money = new Money("123");
        miners.setAmount(money.getValue());
//        iMinersService.add(miners);
    }

    @Test
    public void update() {
        MinerPO miners = new MinerPO();
        miners.setAddress("address");
        miners.setAmount(new Money("234").getValue());
        iMinersService.update(miners);
    }

    @Test
    public void delete() {
        Assert.assertEquals(1,iMinersService.delete("address"));
    }

    @Test
    public void getByField() {
    }

    @Test
    public void findByPage() {
    }
}
