

CREATE TABLE devices (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    device_type VARCHAR(50)  NOT NULL,
    host        VARCHAR(255) NOT NULL,
    location    VARCHAR(255) NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE status_reports (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id  UUID        NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL,
    message    TEXT,
    reported_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status_reports_device_id ON status_reports(device_id);
CREATE INDEX idx_status_reports_reported_at ON status_reports(reported_at DESC);
