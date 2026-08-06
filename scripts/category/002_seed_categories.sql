BEGIN;

INSERT INTO public.category (
    code,
    name,
    description,
    is_active
)
VALUES
    (
        'GIAO_DUC',
        'Giáo dục',
        'Phản ánh và dịch vụ thuộc lĩnh vực giáo dục.',
        TRUE
    ),
    (
        'CONG_AN_CU_TRU',
        'Công an - Cư trú',
        'Thủ tục công an, cư trú và xác nhận thông tin dân cư.',
        TRUE
    ),
    (
        'DICH_VU_CONG_TRUC_TUYEN',
        'Dịch vụ công trực tuyến',
        'Dịch vụ và thủ tục hành chính thực hiện trực tuyến.',
        TRUE
    ),
    (
        'GIAO_THONG',
        'Giao thông',
        'Phản ánh về giao thông và hạ tầng đi lại.',
        TRUE
    ),
    (
        'DANG_KY_KINH_DOANH',
        'Đăng ký kinh doanh',
        'Thủ tục đăng ký và quản lý hoạt động kinh doanh.',
        TRUE
    ),
    (
        'THUE',
        'Thuế',
        'Thủ tục, nghĩa vụ và dịch vụ hành chính về thuế.',
        TRUE
    ),
    (
        'TU_PHAP_HO_TICH',
        'Tư pháp - Hộ tịch',
        'Thủ tục tư pháp, hộ tịch và chứng thực.',
        TRUE
    ),
    (
        'Y_TE_BAO_HIEM_Y_TE',
        'Y tế - Bảo hiểm y tế',
        'Dịch vụ y tế và chính sách bảo hiểm y tế.',
        TRUE
    ),
    (
        'HANH_CHINH_CONG',
        'Hành chính công',
        'Tiếp nhận và xử lý các thủ tục hành chính công.',
        TRUE
    ),
    (
        'DAT_DAI_NHA_O',
        'Đất đai - Nhà ở',
        'Thủ tục và phản ánh liên quan đến đất đai, nhà ở.',
        TRUE
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

COMMIT;
