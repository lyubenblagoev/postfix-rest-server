package com.lyubenblagoev.postfixrest.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "recipient_bccs")
public class IncommingBcc extends Bcc {
}
