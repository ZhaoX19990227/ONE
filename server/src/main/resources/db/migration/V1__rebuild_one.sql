CREATE TABLE user_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    open_id VARCHAR(64) NOT NULL,
    nickname VARCHAR(40) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    height_cm INT NULL,
    weight_gram INT NULL,
    gay_role VARCHAR(16) NULL,
    role_visibility VARCHAR(16) NOT NULL DEFAULT 'PRIVATE',
    meal_preferences JSON NULL,
    drink_preferences JSON NULL,
    dietary_restrictions JSON NULL,
    monthly_budget_fen INT NULL,
    ai_enabled BIT(1) NOT NULL DEFAULT b'1',
    private_habit_enabled BIT(1) NOT NULL DEFAULT b'1',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_open_id (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE catalog_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dimension VARCHAR(20) NOT NULL,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(40) NOT NULL,
    icon VARCHAR(32) NULL,
    color VARCHAR(12) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_dimension_code (dimension, code),
    KEY idx_category_dimension_active (dimension, active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE catalog_brand (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dimension VARCHAR(20) NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(80) NOT NULL,
    short_name VARCHAR(30) NULL,
    logo_url VARCHAR(500) NULL,
    brand_color VARCHAR(12) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_brand_dimension_code (dimension, code),
    KEY idx_brand_dimension_active (dimension, active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE brand_alias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    brand_id BIGINT NOT NULL,
    alias_name VARCHAR(80) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alias_brand FOREIGN KEY (brand_id) REFERENCES catalog_brand (id),
    UNIQUE KEY uk_brand_alias (alias_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE catalog_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dimension VARCHAR(20) NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500) NULL,
    default_price_fen INT NULL,
    attributes JSON NULL,
    base_weight INT NOT NULL DEFAULT 100,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_item_brand FOREIGN KEY (brand_id) REFERENCES catalog_brand (id),
    UNIQUE KEY uk_item_dimension_code (dimension, code),
    KEY idx_item_recommend (dimension, active, category_id, brand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE decision_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    budget_max_fen INT NULL,
    context_json JSON NULL,
    chosen_candidate_id BIGINT NULL,
    actual_record_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_decision_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    KEY idx_decision_user_created (user_id, created_at),
    KEY idx_decision_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE decision_candidate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NULL,
    item_id BIGINT NOT NULL,
    score INT NOT NULL,
    reason_text VARCHAR(500) NOT NULL,
    suggestion_json JSON NULL,
    memory_id BIGINT NULL,
    position_no INT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_candidate_session FOREIGN KEY (session_id) REFERENCES decision_session (id),
    CONSTRAINT fk_candidate_category FOREIGN KEY (category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_candidate_brand FOREIGN KEY (brand_id) REFERENCES catalog_brand (id),
    CONSTRAINT fk_candidate_item FOREIGN KEY (item_id) REFERENCES catalog_item (id),
    UNIQUE KEY uk_candidate_position (session_id, position_no),
    KEY idx_candidate_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE life_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_type VARCHAR(20) NOT NULL,
    record_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    occurred_at DATETIME(3) NOT NULL,
    decision_session_id BIGINT NULL,
    category_id BIGINT NULL,
    brand_id BIGINT NULL,
    item_id BIGINT NULL,
    title VARCHAR(120) NOT NULL,
    brand_name_snapshot VARCHAR(80) NULL,
    item_name_snapshot VARCHAR(100) NULL,
    catalog_match_status VARCHAR(20) NOT NULL DEFAULT 'MATCHED',
    thumbnail_url VARCHAR(500) NULL,
    original_amount_fen INT NULL,
    discount_amount_fen INT NULL,
    actual_amount_fen INT NULL,
    rating INT NULL,
    note VARCHAR(300) NULL,
    source VARCHAR(20) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_record_decision FOREIGN KEY (decision_session_id) REFERENCES decision_session (id),
    CONSTRAINT fk_record_category FOREIGN KEY (category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_record_brand FOREIGN KEY (brand_id) REFERENCES catalog_brand (id),
    CONSTRAINT fk_record_item FOREIGN KEY (item_id) REFERENCES catalog_item (id),
    KEY idx_record_user_time (user_id, occurred_at),
    KEY idx_record_user_type_time (user_id, record_status, record_type, occurred_at),
    KEY idx_record_brand_time (brand_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE media_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    storage_key VARCHAR(300) NOT NULL,
    original_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NULL,
    content_type VARCHAR(80) NOT NULL,
    size_bytes BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'READY',
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_media_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    UNIQUE KEY uk_media_storage_key (storage_key),
    KEY idx_media_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE recognition_task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    media_asset_id BIGINT NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    candidates_json JSON NULL,
    confidence DECIMAL(5,4) NULL,
    confirmed_category_id BIGINT NULL,
    confirmed_brand_id BIGINT NULL,
    confirmed_item_id BIGINT NULL,
    custom_brand_name VARCHAR(80) NULL,
    custom_item_name VARCHAR(100) NULL,
    failure_code VARCHAR(40) NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_recognition_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_recognition_media FOREIGN KEY (media_asset_id) REFERENCES media_asset (id),
    CONSTRAINT fk_recognition_category FOREIGN KEY (confirmed_category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_recognition_brand FOREIGN KEY (confirmed_brand_id) REFERENCES catalog_brand (id),
    CONSTRAINT fk_recognition_item FOREIGN KEY (confirmed_item_id) REFERENCES catalog_item (id),
    KEY idx_recognition_user_status (user_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE catalog_custom_entry (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    category_id BIGINT NULL,
    brand_name VARCHAR(80) NULL,
    item_name VARCHAR(100) NOT NULL,
    normalized_brand_id BIGINT NULL,
    normalized_item_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_custom_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_custom_category FOREIGN KEY (category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_custom_brand FOREIGN KEY (normalized_brand_id) REFERENCES catalog_brand (id),
    CONSTRAINT fk_custom_item FOREIGN KEY (normalized_item_id) REFERENCES catalog_item (id),
    KEY idx_custom_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE meal_record_detail (
    record_id BIGINT NOT NULL,
    dining_mode VARCHAR(20) NULL,
    hunger_level VARCHAR(20) NULL,
    taste_feedback VARCHAR(24) NULL,
    satiety VARCHAR(20) NULL,
    repurchase_intent VARCHAR(20) NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_meal_record FOREIGN KEY (record_id) REFERENCES life_record (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE drink_record_detail (
    record_id BIGINT NOT NULL,
    sugar_level VARCHAR(20) NULL,
    ice_level VARCHAR(20) NULL,
    cup_size VARCHAR(20) NULL,
    toppings JSON NULL,
    taste_feedback VARCHAR(24) NULL,
    repurchase_intent VARCHAR(20) NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_drink_record FOREIGN KEY (record_id) REFERENCES life_record (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE private_habit_record (
    record_id BIGINT NOT NULL,
    ordinal_of_day INT NOT NULL,
    body_feeling VARCHAR(20) NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_habit_record FOREIGN KEY (record_id) REFERENCES life_record (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE preference_memory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    category_id BIGINT NULL,
    brand_id BIGINT NULL,
    item_id BIGINT NULL,
    memory_signal VARCHAR(30) NOT NULL,
    attribute_key VARCHAR(40) NULL,
    observed_value VARCHAR(80) NULL,
    suggested_value VARCHAR(80) NULL,
    display_text VARCHAR(300) NOT NULL,
    source_record_id BIGINT NOT NULL,
    source_at DATETIME(3) NOT NULL,
    strength INT NOT NULL DEFAULT 100,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_memory_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_memory_category FOREIGN KEY (category_id) REFERENCES catalog_category (id),
    CONSTRAINT fk_memory_brand FOREIGN KEY (brand_id) REFERENCES catalog_brand (id),
    CONSTRAINT fk_memory_item FOREIGN KEY (item_id) REFERENCES catalog_item (id),
    CONSTRAINT fk_memory_record FOREIGN KEY (source_record_id) REFERENCES life_record (id),
    KEY idx_memory_item (user_id, dimension, item_id, active, source_at),
    KEY idx_memory_brand (user_id, dimension, brand_id, active, source_at),
    KEY idx_memory_signal (user_id, memory_signal, active, source_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    available_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    processed_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_idempotency (idempotency_key),
    KEY idx_outbox_dispatch (status, available_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO catalog_category (id, dimension, code, name, icon, color, sort_order, active, created_at, updated_at) VALUES
(1,'MEAL','BREAKFAST','早餐','sunrise','#F4B59E',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(2,'MEAL','RICE','米饭快餐','rice','#F8A4A8',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(3,'MEAL','NOODLE','粉面','noodle','#D7A68A',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(4,'MEAL','BURGER','汉堡快餐','burger','#E59A83',40,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(5,'MEAL','HOT_POT','火锅','pot','#CE817F',50,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(6,'MEAL','MALATANG','麻辣烫冒菜','bowl','#D88C74',60,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(7,'MEAL','CHINESE_FAST','中式快餐','dish','#E2B79A',70,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(8,'MEAL','LIGHT','清淡轻食','leaf','#9BC8B2',80,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(20,'MILK_TEA','MILK_TEA','奶茶','cup','#D7B49E',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(21,'MILK_TEA','LIGHT_MILK_TEA','轻乳茶','tea','#C8B7A2',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(22,'MILK_TEA','FRUIT_TEA','果茶','fruit','#F1A89C',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(23,'MILK_TEA','PURE_TEA','纯茶','leaf','#87BCA5',40,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(30,'COFFEE','AMERICANO','美式','coffee','#8C6D5A',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(31,'COFFEE','LATTE','拿铁奶咖','latte','#B9997D',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(32,'COFFEE','FLAVORED_COFFEE','风味咖啡','spark','#A88578',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3));

INSERT INTO catalog_brand (id, dimension, code, name, short_name, brand_color, sort_order, active, created_at, updated_at) VALUES
(1,'MEAL','MCDONALDS','麦当劳','M','#FFC72C',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(2,'MEAL','KFC','肯德基','KFC','#D71920',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(3,'MEAL','HAIDILAO','海底捞','海','#D9251D',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(4,'MEAL','YANGGUOFU','杨国福麻辣烫','杨','#D84A37',40,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(5,'MEAL','ZHANGLIANG','张亮麻辣烫','张','#E07A3F',50,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(6,'MEAL','TASTIEN','塔斯汀','塔','#C72C2C',60,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(7,'MEAL','LAOXIANGJI','老乡鸡','鸡','#D64D3C',70,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(20,'MILK_TEA','HEYTEA','喜茶','喜','#111111',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(21,'MILK_TEA','CHAGEE','霸王茶姬','茶姬','#9B1C31',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(22,'MILK_TEA','MIXUE','蜜雪冰城','蜜雪','#E60012',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(23,'MILK_TEA','CHABAIDAO','茶百道','茶百道','#F5C400',40,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(24,'MILK_TEA','AUNTEA_JENNY','沪上阿姨','沪上','#D9272E',50,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(25,'MILK_TEA','NAYUKI','奈雪的茶','奈雪','#1F513F',60,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(26,'MILK_TEA','GUMING','古茗','古茗','#E85035',70,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(30,'COFFEE','STARBUCKS','星巴克','星巴克','#00754A',10,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(31,'COFFEE','MANNER','Manner Coffee','Manner','#222222',20,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(32,'COFFEE','MSTAND','M Stand','M Stand','#111111',30,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(33,'COFFEE','LUCKIN','瑞幸咖啡','瑞幸','#102C74',40,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(34,'COFFEE','COTTI','库迪咖啡','库迪','#C91C2B',50,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3));

INSERT INTO catalog_item (id, dimension, category_id, brand_id, code, name, default_price_fen, attributes, base_weight, active, created_at, updated_at) VALUES
(1,'MEAL',4,1,'MCD_SPICY_CHICKEN','麦辣鸡腿堡套餐',3200,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('重口','快捷')),105,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(2,'MEAL',4,2,'KFC_SPICY_BURGER','香辣鸡腿堡套餐',3300,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('重口','快捷')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(3,'MEAL',5,3,'HDL_HOTPOT','海底捞火锅',12000,JSON_OBJECT('mealSlots',JSON_ARRAY('DINNER','LATE_NIGHT'),'tags',JSON_ARRAY('聚餐','热乎')),85,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(4,'MEAL',6,4,'YGF_MALATANG','自选麻辣烫',3200,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('有汤','自选')),120,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(5,'MEAL',6,5,'ZL_MALATANG','自选麻辣烫',3000,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('有汤','自选')),115,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(6,'MEAL',4,6,'TST_CHINESE_BURGER','中国汉堡套餐',2800,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('快捷')),105,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(7,'MEAL',7,7,'LXJ_CHICKEN_SOUP','肥西老母鸡汤套餐',4200,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('米饭','有汤')),110,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(8,'MEAL',3,NULL,'BEEF_NOODLE','牛肉面',2800,JSON_OBJECT('mealSlots',JSON_ARRAY('BREAKFAST','LUNCH','DINNER'),'tags',JSON_ARRAY('粉面','有汤')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(9,'MEAL',2,NULL,'BRAISED_CHICKEN_RICE','黄焖鸡米饭',3000,JSON_OBJECT('mealSlots',JSON_ARRAY('LUNCH','DINNER'),'tags',JSON_ARRAY('米饭','下饭')),110,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(20,'MILK_TEA',22,20,'HEYTEA_GRAPE','多肉葡萄',2200,JSON_OBJECT('sugarOptions',JSON_ARRAY('NO_SUGAR','THREE','FIVE','SEVEN'),'iceOptions',JSON_ARRAY('NO_ICE','LESS','NORMAL')),110,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(21,'MILK_TEA',21,21,'CHAGEE_BOYA','伯牙绝弦',1800,JSON_OBJECT('sugarOptions',JSON_ARRAY('NO_SUGAR','LOW','NORMAL'),'iceOptions',JSON_ARRAY('NO_ICE','LESS','NORMAL')),125,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(22,'MILK_TEA',20,22,'MIXUE_BOBA','珍珠奶茶',800,JSON_OBJECT('sugarOptions',JSON_ARRAY('THREE','FIVE','SEVEN','FULL'),'iceOptions',JSON_ARRAY('NO_ICE','LESS','NORMAL')),95,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(23,'MILK_TEA',22,23,'CHABAIDAO_MANGO','杨枝甘露',1800,JSON_OBJECT('sugarOptions',JSON_ARRAY('THREE','FIVE','SEVEN'),'iceOptions',JSON_ARRAY('LESS','NORMAL')),105,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(24,'MILK_TEA',21,24,'AUNTEA_JASMINE','茉莉轻乳茶',1600,JSON_OBJECT('sugarOptions',JSON_ARRAY('NO_SUGAR','THREE','FIVE'),'iceOptions',JSON_ARRAY('NO_ICE','LESS','NORMAL')),110,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(25,'MILK_TEA',22,25,'NAYUKI_GRAPE','霸气葡萄',2300,JSON_OBJECT('sugarOptions',JSON_ARRAY('THREE','FIVE','SEVEN'),'iceOptions',JSON_ARRAY('LESS','NORMAL')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(26,'MILK_TEA',20,26,'GUMING_MILK_TEA','古茗奶茶',1500,JSON_OBJECT('sugarOptions',JSON_ARRAY('THREE','FIVE','SEVEN'),'iceOptions',JSON_ARRAY('NO_ICE','LESS','NORMAL')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(30,'COFFEE',31,30,'STARBUCKS_LATTE','拿铁',3200,JSON_OBJECT('temperatureOptions',JSON_ARRAY('HOT','ICED'),'tags',JSON_ARRAY('奶咖')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(31,'COFFEE',30,31,'MANNER_AMERICANO','冰美式',1500,JSON_OBJECT('temperatureOptions',JSON_ARRAY('HOT','ICED'),'tags',JSON_ARRAY('黑咖')),120,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(32,'COFFEE',31,32,'MSTAND_COCONUT_LATTE','椰青冰萃',2800,JSON_OBJECT('temperatureOptions',JSON_ARRAY('ICED'),'tags',JSON_ARRAY('风味','清爽')),105,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(33,'COFFEE',31,33,'LUCKIN_COCONUT_LATTE','生椰拿铁',1800,JSON_OBJECT('temperatureOptions',JSON_ARRAY('ICED'),'tags',JSON_ARRAY('奶咖','椰香')),115,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3)),
(34,'COFFEE',30,34,'COTTI_AMERICANO','冰美式',1200,JSON_OBJECT('temperatureOptions',JSON_ARRAY('HOT','ICED'),'tags',JSON_ARRAY('黑咖')),100,b'1',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3));

INSERT INTO brand_alias (brand_id, alias_name, created_at) VALUES
(1,'McDonald''s',UTC_TIMESTAMP(3)),(1,'M记',UTC_TIMESTAMP(3)),
(2,'KFC',UTC_TIMESTAMP(3)),(2,'开封菜',UTC_TIMESTAMP(3)),
(20,'HEYTEA',UTC_TIMESTAMP(3)),(21,'CHAGEE',UTC_TIMESTAMP(3)),
(22,'蜜雪',UTC_TIMESTAMP(3)),(24,'AUNTEA JENNY',UTC_TIMESTAMP(3)),
(25,'奈雪',UTC_TIMESTAMP(3)),(30,'Starbucks',UTC_TIMESTAMP(3)),
(31,'Manner',UTC_TIMESTAMP(3)),(32,'MStand',UTC_TIMESTAMP(3)),
(33,'Luckin',UTC_TIMESTAMP(3)),(34,'Cotti',UTC_TIMESTAMP(3));
