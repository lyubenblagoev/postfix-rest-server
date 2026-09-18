package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipient_bccs")
public class IncomingBcc extends Bcc {
}
