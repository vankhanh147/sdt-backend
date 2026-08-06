-- Development/test only: remove exactly the ten Task 008 seed categories.
BEGIN;

DELETE FROM public.category
WHERE code IN (
    'GIAO_DUC',
    'CONG_AN_CU_TRU',
    'DICH_VU_CONG_TRUC_TUYEN',
    'GIAO_THONG',
    'DANG_KY_KINH_DOANH',
    'THUE',
    'TU_PHAP_HO_TICH',
    'Y_TE_BAO_HIEM_Y_TE',
    'HANH_CHINH_CONG',
    'DAT_DAI_NHA_O'
);

COMMIT;
