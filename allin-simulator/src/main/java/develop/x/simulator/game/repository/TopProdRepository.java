package develop.x.simulator.game.repository;

import develop.x.simulator.game.entity.SaleType;
import develop.x.simulator.game.entity.TopProd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopProdRepository extends JpaRepository<TopProd, Long> {

    List<TopProd> findByProductTypeAndSaleType(String productType, SaleType saleType);

}