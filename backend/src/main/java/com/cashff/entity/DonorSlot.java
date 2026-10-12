package com.cashff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "donor_slots", uniqueConstraints = @UniqueConstraint(columnNames = {"slot_name", "rank_label"}))
public class DonorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_name", nullable = false, length = 30)
    private String slotName;

    @Column(name = "rank_label", nullable = false, length = 10)
    private String rankLabel;

    @Column(nullable = false, length = 100)
    private String supporterName = "Gamer";

    @Column(nullable = false, length = 50)
    private String amount = "₹100";

    @Column(nullable = false, length = 50)
    private String type = "YouTube";

    @Column(nullable = false, length = 500)
    private String link = "#";

    public DonorSlot() {
    }

    public DonorSlot(String slotName, String rankLabel, String name, String amount, String type, String link) {
        this.slotName = slotName;
        this.rankLabel = rankLabel;
        this.supporterName = name;
        this.amount = amount;
        this.type = type;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getRankLabel() {
        return rankLabel;
    }

    public void setRankLabel(String rankLabel) {
        this.rankLabel = rankLabel;
    }

    public String getSupporterName() {
        return supporterName;
    }

    public void setSupporterName(String supporterName) {
        this.supporterName = supporterName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
