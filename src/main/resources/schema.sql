
CREATE TABLE IF NOT EXISTS load_attempts (
    id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    load_amount DECIMAL(19, 2) NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    accepted BOOLEAN NOT NULL,
    -- This constraint ignores duplicates for the same ID and Customer
    PRIMARY KEY (id, customer_id)
);

-- Index for performance when checking daily/weekly limits
CREATE INDEX idx_customer_time ON load_attempts(customer_id, attempt_time);