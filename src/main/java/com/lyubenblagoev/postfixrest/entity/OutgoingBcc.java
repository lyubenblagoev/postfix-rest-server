package com.lyubenblagoev.postfixrest.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "sender_bccs")
public class OutgoingBcc extends Bcc {
}
