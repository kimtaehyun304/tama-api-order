package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    // 우편번호
    @Column(nullable = false)
    private String zipCode;

    // 도로명 주소
    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String detail;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String receiverNickname;

    @Column(nullable = false)
    private String receiverPhone;

    //배송 상태는 주문 상태에서 관리

    public Delivery(String zipCode, String street, String detail, String message, String receiverNickname, String receiverPhone) {
        this.zipCode = zipCode;
        this.street = street;
        this.detail = detail;
        this.message = message;
        this.receiverNickname = receiverNickname;
        this.receiverPhone = receiverPhone;
    }

    public void setIdByBatchId(Long id) {
        this.id = id;
    }
}


