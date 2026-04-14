-- 供应商统一社会信用代码取消唯一约束，仅保留供应商编码唯一

drop index if exists uk_supply_supplier_credit_code;

