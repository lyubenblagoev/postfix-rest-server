package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sender_bccs")
public class OutgoingBcc extends Bcc {
}
