package develop.x.simulator.game.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class TopProd {

    @Id
    private String productCode;

    private String productName;

    private String productType;

    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    @OneToMany(fetch = FetchType.LAZY,  mappedBy = "topProd")  // 다대일 관계
    private List<TopLlEvnt> topLlEvnt = new ArrayList<>();  // 연관된 제품 객체

    // 기본 생성자
    public TopProd() {}

    // 생성자
    @Builder
    public TopProd(String productName, String productCode, String productType, SaleType saleType) {
        this.productName = productName;
        this.productCode = productCode;
        this.productType = productType;
        this.saleType = saleType;
    }

    // Getter, Setter 생략
}