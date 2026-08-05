-- Task 008 read-only API seed data generated from data/feedback_llm.csv.
-- Purpose: exercise pagination, sorting, keyword/category/source/status filters,
--          nullable analysis fields, and latest AnalysisResult selection.
-- Review this script before running it manually. It is never run automatically.

BEGIN;

-- Idempotent cleanup restricted to the dedicated seed prefix.
DELETE FROM public.analysis_result AS analysis
USING public.feedback AS feedback,
      public.raw_feedback AS raw
WHERE analysis.feedback_id = feedback.id
  AND feedback.raw_feedback_id = raw.id
  AND raw.source_ref LIKE 'SEED-LLM-%';

DELETE FROM public.feedback AS feedback
USING public.raw_feedback AS raw
WHERE feedback.raw_feedback_id = raw.id
  AND raw.source_ref LIKE 'SEED-LLM-%';

DELETE FROM public.raw_feedback
WHERE source_ref LIKE 'SEED-LLM-%';

WITH seed_rows (
    seed_order,
    csv_id,
    linh_vuc,
    dich_vu_cu_the,
    khia_canh_chinh,
    kenh_phan_hoi,
    diem_danh_gia,
    nhan_cam_xuc,
    noi_dung_phan_hoi
) AS (
    VALUES
        (0, 526, $seed$Giáo dục$seed$, $seed$cấp bảng điểm học tập$seed$, $seed$Cơ sở vật chất$seed$, $seed$Khảo sát trực tuyến$seed$, 5, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với quy trình cấp bảng điểm tại trường. Không gian chờ đợi tại văn phòng một cửa rất thoáng mát, sạch sẽ và hiện đại, giúp tôi cảm thấy thoải mái trong khi chờ đợi nhận kết quả.$seed$),
        (1, 170, $seed$Công an - Cư trú$seed$, $seed$đăng ký tạm trú$seed$, $seed$Cơ sở vật chất$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 4, $seed$Tích cực$seed$, $seed$Phòng chờ tại nơi tiếp nhận hồ sơ đăng ký tạm trú rất sạch sẽ và thoáng mát. Tôi cảm thấy thoải mái khi chờ đợi làm thủ tục. Tuy nhiên, nếu cơ quan có thêm vài chiếc ghế ngồi cho người dân thì sẽ tuyệt vời hơn nữa.$seed$),
        (2, 185, $seed$Dịch vụ công trực tuyến$seed$, $seed$nộp hồ sơ qua Cổng Dịch vụ công Quốc gia$seed$, $seed$Hệ thống trực tuyến$seed$, $seed$Tổng đài chăm sóc$seed$, 5, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với việc nộp hồ sơ trực tuyến qua Cổng Dịch vụ công Quốc gia. Hệ thống hoạt động mượt mà, giao diện dễ nhìn và các bước thực hiện rất đơn giản. Tôi đánh giá cao sự cải tiến này vì giúp tôi tiết kiệm được rất nhiều thời gian và công sức đi lại.$seed$),
        (3, 339, $seed$Giao thông$seed$, $seed$đổi bằng lái xe$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Mạng xã hội$seed$, 4, $seed$Tích cực$seed$, $seed$Mình vừa đi đổi bằng lái xe xong, thủ tục hồ sơ hiện nay đã đơn giản hơn nhiều so với trước đây. Cán bộ hướng dẫn rất chi tiết và nhiệt tình nên mình hoàn thành nhanh chóng. Rất hài lòng với sự cải thiện này của cơ quan chức năng.$seed$),
        (4, 22, $seed$Đăng ký kinh doanh$seed$, $seed$thay đổi nội dung đăng ký kinh doanh$seed$, $seed$Thái độ cán bộ$seed$, $seed$Khảo sát trực tuyến$seed$, 5, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với thái độ phục vụ của cán bộ tiếp nhận hồ sơ thay đổi đăng ký kinh doanh. Các anh chị hướng dẫn rất tận tình, chu đáo và lịch sự, giúp tôi hoàn thành thủ tục nhanh chóng mà không gặp khó khăn gì.$seed$),
        (5, 724, $seed$Thuế$seed$, $seed$kê khai thuế hộ kinh doanh$seed$, $seed$Thời gian xử lý$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 4, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với quy trình kê khai thuế cho hộ kinh doanh tại đây. Nhân viên hướng dẫn tận tình và thời gian xử lý hồ sơ nhanh hơn tôi mong đợi, giúp tôi tiết kiệm được nhiều thời gian đi lại.$seed$),
        (6, 28, $seed$Tư pháp - Hộ tịch$seed$, $seed$cấp giấy xác nhận tình trạng hôn nhân$seed$, $seed$Thái độ cán bộ$seed$, $seed$Tổng đài chăm sóc$seed$, 5, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với thái độ phục vụ của cán bộ tại bộ phận một cửa. Anh ấy hướng dẫn tôi làm thủ tục cấp giấy xác nhận tình trạng hôn nhân rất tận tình, chu đáo và lịch sự. Quy trình giải quyết hồ sơ nhanh chóng và chuyên nghiệp, khiến tôi cảm thấy rất thoải mái.$seed$),
        (7, 103, $seed$Y tế - Bảo hiểm y tế$seed$, $seed$khám chữa bệnh BHYT$seed$, $seed$Chi phí lệ phí$seed$, $seed$Mạng xã hội$seed$, 4, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với mức chi phí khám chữa bệnh BHYT hiện nay vì rất hợp lý và giúp giảm bớt gánh nặng tài chính cho gia đình. Thủ tục thanh toán nhanh chóng, minh bạch, không hề có chi phí phát sinh ngoài quy định. Mong bệnh viện tiếp tục duy trì chất lượng dịch vụ tốt như vậy.$seed$),
        (8, 244, $seed$Hành chính công$seed$, $seed$cấp CCCD gắn chip$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Khảo sát trực tuyến$seed$, 5, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với quy trình cấp CCCD gắn chip tại địa phương. Các bước hướng dẫn chuẩn bị hồ sơ rất rõ ràng và chi tiết, giúp tôi hoàn thành thủ tục một cách nhanh chóng mà không phải đi lại nhiều lần.$seed$),
        (9, 449, $seed$Đất đai - Nhà ở$seed$, $seed$tách thửa đất$seed$, $seed$Thời gian xử lý$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 4, $seed$Tích cực$seed$, $seed$Tôi rất hài lòng với quy trình tách thửa đất tại đây. Thời gian xử lý hồ sơ nhanh hơn nhiều so với tôi mong đợi, cán bộ hướng dẫn rất tận tình. Hy vọng đơn vị tiếp tục duy trì và cải thiện chất lượng phục vụ như thế này.$seed$),
        (10, 112, $seed$Giáo dục$seed$, $seed$chuyển trường$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi thấy quy trình chuyển trường cho con hiện tại đã rõ ràng hơn trước, tuy nhiên thời gian chờ đợi phê duyệt hồ sơ vẫn còn hơi lâu. Nếu cơ quan chức năng có thể rút ngắn bớt các bước trung gian thì người dân sẽ thuận tiện hơn trong việc hoàn tất thủ tục.$seed$),
        (11, 221, $seed$Công an - Cư trú$seed$, $seed$đăng ký thường trú$seed$, $seed$Chi phí lệ phí$seed$, $seed$Tổng đài chăm sóc$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi vừa hoàn thành thủ tục đăng ký thường trú qua tổng đài hướng dẫn. Mọi thứ diễn ra khá ổn, tuy nhiên mức lệ phí hiện tại tôi thấy hơi cao so với mặt bằng chung. Hy vọng cơ quan chức năng có thể xem xét điều chỉnh mức phí này để hỗ trợ người dân tốt hơn.$seed$),
        (12, 36, $seed$Dịch vụ công trực tuyến$seed$, $seed$tra cứu tiến độ hồ sơ trực tuyến$seed$, $seed$Hệ thống trực tuyến$seed$, $seed$Mạng xã hội$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi đã thử tra cứu tiến độ hồ sơ trực tuyến nhưng hệ thống đôi khi load chậm và giao diện hơi khó nhìn. Tuy nhiên, việc cung cấp thông tin cập nhật là khá đầy đủ và chính xác. Mong cơ quan chức năng sớm nâng cấp giao diện để người dân dễ dàng thao tác hơn.$seed$),
        (13, 107, $seed$Giao thông$seed$, $seed$đăng ký xe máy$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Khảo sát trực tuyến$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi đã hoàn thành việc đăng ký xe máy tại cơ quan chức năng. Về cơ bản các bước thực hiện đúng theo hướng dẫn nhưng tôi thấy khâu chuẩn bị hồ sơ vẫn còn hơi rườm rà và mất thời gian chờ đợi. Hy vọng đơn vị quản lý sẽ sớm cải thiện quy trình để người dân thuận tiện hơn.$seed$),
        (14, 59, $seed$Đăng ký kinh doanh$seed$, $seed$thành lập doanh nghiệp$seed$, $seed$Chi phí lệ phí$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 3, $seed$Trung lập$seed$, $seed$Chi phí lệ phí khi làm thủ tục thành lập doanh nghiệp ở mức chấp nhận được, không quá cao. Tuy nhiên, tôi thấy quy trình hướng dẫn đóng phí vẫn còn hơi chậm và chưa thực sự rõ ràng cho người mới.$seed$),
        (15, 171, $seed$Thuế$seed$, $seed$nộp thuế điện tử$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Tổng đài chăm sóc$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi đã thử nộp thuế điện tử nhưng thấy các bước hướng dẫn trong hồ sơ còn hơi phức tạp và khó hiểu. Nhìn chung thì hệ thống vận hành ổn định nhưng tôi hy vọng cơ quan thuế sẽ đơn giản hóa các thủ tục này để người dân dễ dàng thao tác hơn.$seed$),
        (16, 46, $seed$Tư pháp - Hộ tịch$seed$, $seed$đăng ký kết hôn$seed$, $seed$Cơ sở vật chất$seed$, $seed$Mạng xã hội$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi vừa hoàn thành thủ tục đăng ký kết hôn tại ủy ban. Mọi thứ diễn ra khá ổn nhưng phòng chờ hơi chật và nóng, cần được cải thiện về máy lạnh để người dân thoải mái hơn khi chờ đợi.$seed$),
        (17, 472, $seed$Y tế - Bảo hiểm y tế$seed$, $seed$thanh toán chi phí khám chữa bệnh$seed$, $seed$Hệ thống trực tuyến$seed$, $seed$Khảo sát trực tuyến$seed$, 3, $seed$Trung lập$seed$, $seed$Hệ thống thanh toán trực tuyến hiện tại hoạt động tương đối ổn định, tuy nhiên giao diện vẫn còn hơi khó sử dụng đối với người lớn tuổi. Tôi hy vọng cơ quan quản lý có thể cải tiến quy trình thao tác để việc quyết toán chi phí khám chữa bệnh trở nên nhanh chóng và thuận tiện hơn.$seed$),
        (18, 276, $seed$Hành chính công$seed$, $seed$cấp CCCD gắn chip$seed$, $seed$Cơ sở vật chất$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi thấy quy trình làm thẻ căn cước công dân gắn chip khá nhanh chóng. Tuy nhiên, khu vực chờ tại quầy có hơi ít ghế ngồi khiến nhiều người phải đứng đợi lâu. Hy vọng cơ quan chức năng sớm cải thiện cơ sở vật chất để người dân thoải mái hơn.$seed$),
        (19, 214, $seed$Đất đai - Nhà ở$seed$, $seed$cấp giấy chứng nhận quyền sử dụng đất (sổ đỏ)$seed$, $seed$Thái độ cán bộ$seed$, $seed$Tổng đài chăm sóc$seed$, 3, $seed$Trung lập$seed$, $seed$Tôi đã đến làm thủ tục cấp sổ đỏ và thấy cán bộ hướng dẫn tương đối đầy đủ. Tuy nhiên, thái độ làm việc đôi khi còn hơi cứng nhắc và chưa thực sự niềm nở với người dân. Tôi hy vọng cơ quan chức năng sẽ cải thiện phong cách phục vụ để người dân cảm thấy thoải mái hơn.$seed$),
        (20, 42, $seed$Giáo dục$seed$, $seed$xét miễn giảm học phí$seed$, $seed$Thời gian xử lý$seed$, $seed$Tổng đài chăm sóc$seed$, 1, $seed$Tiêu cực$seed$, $seed$Tôi thực sự thất vọng về thời gian xử lý hồ sơ miễn giảm học phí của trường. Tôi đã nộp đơn từ tháng trước nhưng đến nay vẫn chưa nhận được kết quả, trong khi thời hạn đóng tiền học đã cận kề. Cách làm việc chậm chạp thế này gây khó khăn rất lớn cho gia đình tôi.$seed$),
        (21, 155, $seed$Công an - Cư trú$seed$, $seed$đăng ký thường trú$seed$, $seed$Chi phí lệ phí$seed$, $seed$Mạng xã hội$seed$, 2, $seed$Tiêu cực$seed$, $seed$Tôi thấy mức phí đăng ký thường trú hiện nay quá cao so với mức thu nhập của người dân. Mong cơ quan chức năng xem xét giảm bớt các khoản lệ phí này để hỗ trợ mọi người làm thủ tục nhanh chóng hơn.$seed$),
        (22, 569, $seed$Dịch vụ công trực tuyến$seed$, $seed$nộp hồ sơ qua Cổng Dịch vụ công Quốc gia$seed$, $seed$Hệ thống trực tuyến$seed$, $seed$Khảo sát trực tuyến$seed$, 1, $seed$Tiêu cực$seed$, $seed$Tôi rất thất vọng với hệ thống nộp hồ sơ trực tuyến vì thường xuyên bị treo và báo lỗi kết nối. Quá trình thao tác rất chậm chạp, khiến tôi mất nhiều thời gian nhưng cuối cùng vẫn không nộp được đơn. Đề nghị cơ quan chức năng sớm nâng cấp hệ thống để người dân dễ dàng tiếp cận hơn.$seed$),
        (23, 50, $seed$Giao thông$seed$, $seed$cấp lại biển số xe$seed$, $seed$Hệ thống trực tuyến$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 2, $seed$Tiêu cực$seed$, $seed$Tôi thấy hệ thống đăng ký cấp lại biển số trực tuyến quá chậm và thường xuyên bị lỗi kết nối. Tôi đã mất cả buổi sáng chỉ để tải lên giấy tờ nhưng vẫn không hoàn thành được hồ sơ. Hy vọng cơ quan chức năng sớm nâng cấp hạ tầng công nghệ để người dân bớt vất vả hơn.$seed$),
        (24, 270, $seed$Đăng ký kinh doanh$seed$, $seed$thành lập doanh nghiệp$seed$, $seed$Thái độ cán bộ$seed$, $seed$Tổng đài chăm sóc$seed$, 1, $seed$Tiêu cực$seed$, $seed$Tôi thực sự thất vọng về thái độ của cán bộ tiếp nhận hồ sơ thành lập doanh nghiệp. Nhân viên làm việc thiếu chuyên nghiệp, gắt gỏng và không hề hướng dẫn tận tình cho người dân. Tôi đề nghị cơ quan chức năng cần chấn chỉnh lại tác phong làm việc của đội ngũ này.$seed$),
        (25, 204, $seed$Thuế$seed$, $seed$quyết toán thuế thu nhập cá nhân$seed$, $seed$Thái độ cán bộ$seed$, $seed$Mạng xã hội$seed$, 2, $seed$Tiêu cực$seed$, $seed$Tôi rất thất vọng với thái độ của cán bộ hướng dẫn quyết toán thuế thu nhập cá nhân. Họ làm việc thiếu nhiệt tình và trả lời hời hợt, khiến tôi mất nhiều thời gian chờ đợi mà không giải quyết được vấn đề. Mong cơ quan thuế sớm chấn chỉnh lại tác phong làm việc của nhân viên.$seed$),
        (26, 183, $seed$Tư pháp - Hộ tịch$seed$, $seed$cấp giấy xác nhận tình trạng hôn nhân$seed$, $seed$Thủ tục hồ sơ$seed$, $seed$Khảo sát trực tuyến$seed$, 1, $seed$Tiêu cực$seed$, $seed$Tôi rất thất vọng vì thủ tục cấp giấy xác nhận tình trạng hôn nhân quá rườm rà. Nhân viên hướng dẫn không rõ ràng khiến tôi phải đi lại nhiều lần để bổ sung giấy tờ mà không được giải thích cặn kẽ. Quy trình làm việc thiếu chuyên nghiệp và gây mất thời gian cho người dân.$seed$),
        (27, 39, $seed$Y tế - Bảo hiểm y tế$seed$, $seed$gia hạn thẻ BHYT$seed$, $seed$Thái độ cán bộ$seed$, $seed$Phiếu khảo sát tại quầy$seed$, 2, $seed$Tiêu cực$seed$, $seed$Tôi rất không hài lòng với thái độ của cán bộ tiếp nhận hồ sơ gia hạn thẻ bảo hiểm y tế. Nhân viên làm việc thiếu nhiệt tình, trả lời cộc lốc và không hướng dẫn chi tiết khiến tôi mất nhiều thời gian chờ đợi. Hy vọng cơ quan quản lý sớm chấn chỉnh lại tác phong làm việc của đội ngũ nhân sự tại đây.$seed$),
        (28, 599, $seed$Hành chính công$seed$, $seed$cấp giấy xác nhận cư trú$seed$, $seed$Thái độ cán bộ$seed$, $seed$Tổng đài chăm sóc$seed$, 1, $seed$Tiêu cực$seed$, $seed$Tôi rất không hài lòng với thái độ của cán bộ tiếp nhận hồ sơ cấp giấy xác nhận cư trú. Nhân viên làm việc hời hợt, trả lời cộc lốc và thiếu tôn trọng người dân khi tôi thắc mắc về thủ tục. Cách làm việc thiếu chuyên nghiệp như vậy khiến tôi cảm thấy rất bức xúc và mệt mỏi.$seed$),
        (29, 1115, $seed$Đất đai - Nhà ở$seed$, $seed$chuyển nhượng quyền sử dụng đất$seed$, $seed$Chi phí lệ phí$seed$, $seed$Mạng xã hội$seed$, 2, $seed$Tiêu cực$seed$, $seed$Tôi thực sự không hài lòng với các khoản phí khi làm thủ tục chuyển nhượng đất. Các loại lệ phí không được niêm yết rõ ràng khiến tôi cảm thấy mập mờ và quá cao so với thực tế. Mong cơ quan chức năng sớm rà soát lại để người dân dễ dàng theo dõi và thực hiện.$seed$)
),
normalized_seed AS (
    SELECT
        seed_rows.*,
        ('10000000-0000-4000-8000-' || lpad(csv_id::text, 12, '0'))::uuid AS raw_id,
        ('20000000-0000-4000-8000-' || lpad(csv_id::text, 12, '0'))::uuid AS feedback_id,
        ('30000000-0000-4000-8000-' || lpad(csv_id::text, 12, '0'))::uuid AS analysis_id,
        TIMESTAMPTZ '2026-07-01 08:00:00+07:00'
            + seed_order * INTERVAL '1 day' AS received_at,
        CASE kenh_phan_hoi
            WHEN 'Khảo sát trực tuyến' THEN 'WEBSITE'
            WHEN 'Phiếu khảo sát tại quầy' THEN 'MANUAL'
            ELSE 'OTHER'
        END AS source,
        CASE nhan_cam_xuc
            WHEN 'Tích cực' THEN 'POSITIVE'
            WHEN 'Trung lập' THEN 'NEUTRAL'
            WHEN 'Tiêu cực' THEN 'NEGATIVE'
        END AS sentiment,
        CASE diem_danh_gia
            WHEN 5 THEN 0.9000
            WHEN 4 THEN 0.8200
            WHEN 3 THEN 0.7000
            WHEN 2 THEN 0.8200
            WHEN 1 THEN 0.9000
        END::numeric(5,4) AS sentiment_score,
        CASE
            WHEN csv_id IN (42, 569, 599) THEN 'URGENT'
            WHEN nhan_cam_xuc = 'Tiêu cực' AND diem_danh_gia = 1 THEN 'HIGH'
            WHEN nhan_cam_xuc = 'Tiêu cực' AND diem_danh_gia = 2
                AND csv_id IN (50, 204, 39) THEN 'HIGH'
            WHEN nhan_cam_xuc = 'Tiêu cực' THEN 'MEDIUM'
            WHEN nhan_cam_xuc = 'Trung lập' THEN 'MEDIUM'
            ELSE 'LOW'
        END AS priority,
        CASE
            WHEN csv_id IN (449, 214, 1115) THEN 'PENDING_ANALYSIS'
            ELSE 'ANALYZED'
        END AS feedback_status
    FROM seed_rows
),
inserted_raw AS (
    INSERT INTO public.raw_feedback (
        id,
        source,
        source_ref,
        raw_title,
        raw_content,
        raw_author_name,
        raw_author_contact,
        raw_location,
        category_hint,
        raw_metadata,
        received_at,
        processing_status,
        created_at,
        updated_at,
        processed_at
    )
    SELECT
        raw_id,
        source,
        'SEED-LLM-' || csv_id,
        COALESCE(NULLIF(dich_vu_cu_the, ''), linh_vuc),
        noi_dung_phan_hoi,
        NULL,
        NULL,
        NULL,
        linh_vuc,
        jsonb_build_object(
            'originalCsvId', csv_id,
            'dichVuCuThe', dich_vu_cu_the,
            'khiaCanhChinh', khia_canh_chinh,
            'kenhPhanHoi', kenh_phan_hoi,
            'diemDanhGia', diem_danh_gia
        ),
        received_at,
        'PROCESSED',
        received_at + INTERVAL '5 minutes',
        received_at + INTERVAL '15 minutes',
        received_at + INTERVAL '15 minutes'
    FROM normalized_seed
    RETURNING id, source_ref
),
inserted_feedback AS (
    INSERT INTO public.feedback (
        id,
        raw_feedback_id,
        title,
        content,
        author_name,
        author_contact,
        location,
        category,
        status,
        created_at,
        updated_at,
        resolved_at
    )
    SELECT
        seed.feedback_id,
        raw.id,
        COALESCE(NULLIF(seed.dich_vu_cu_the, ''), seed.linh_vuc),
        seed.noi_dung_phan_hoi,
        NULL,
        NULL,
        NULL,
        seed.linh_vuc,
        seed.feedback_status,
        seed.received_at + INTERVAL '20 minutes',
        seed.received_at + INTERVAL '20 minutes',
        NULL
    FROM normalized_seed AS seed
    JOIN inserted_raw AS raw
      ON raw.source_ref = 'SEED-LLM-' || seed.csv_id
    RETURNING id, raw_feedback_id, status
),
analysis_seed AS (
    SELECT seed.*, feedback.status
    FROM normalized_seed AS seed
    JOIN inserted_feedback AS feedback
      ON feedback.id = seed.feedback_id
),
analysis_rows AS (
    SELECT
        analysis_id AS id,
        feedback_id,
        sentiment,
        sentiment_score,
        linh_vuc AS category,
        (0.7800 + (seed_order % 5) * 0.0400)::numeric(5,4) AS category_score,
        to_jsonb(ARRAY[khia_canh_chinh, dich_vu_cu_the]) AS matched_keywords,
        priority,
        CASE priority
            WHEN 'LOW' THEN 1
            WHEN 'MEDIUM' THEN 2
            WHEN 'HIGH' THEN 3
            WHEN 'URGENT' THEN 4
        END AS priority_score,
        CASE priority
            WHEN 'LOW' THEN 'Positive feedback; routine follow-up.'
            WHEN 'MEDIUM' THEN 'Neutral or moderately negative feedback; review recommended.'
            WHEN 'HIGH' THEN 'Negative feedback with a low rating; prompt review required.'
            WHEN 'URGENT' THEN 'Severe negative impact described; urgent review required.'
        END AS priority_reason,
        received_at + INTERVAL '110 minutes' AS analyzed_at,
        received_at + INTERVAL '120 minutes' AS created_at,
        received_at + INTERVAL '120 minutes' AS updated_at
    FROM analysis_seed
    WHERE status = 'ANALYZED'

    UNION ALL

    -- Older results for two records; the regular rows above remain the latest.
    SELECT
        ('40000000-0000-4000-8000-' || lpad(csv_id::text, 12, '0'))::uuid,
        feedback_id,
        'NEUTRAL',
        0.6500::numeric(5,4),
        linh_vuc,
        0.7600::numeric(5,4),
        to_jsonb(ARRAY['historical analysis', khia_canh_chinh]),
        'MEDIUM',
        2,
        'Historical seed result retained to verify latest-result selection.',
        received_at + INTERVAL '50 minutes',
        received_at + INTERVAL '60 minutes',
        received_at + INTERVAL '60 minutes'
    FROM analysis_seed
    WHERE csv_id IN (526, 42)
      AND status = 'ANALYZED'
)
INSERT INTO public.analysis_result (
    id,
    feedback_id,
    sentiment,
    sentiment_score,
    category,
    category_score,
    matched_keywords,
    priority,
    priority_score,
    priority_reason,
    model_name,
    model_version,
    analysis_status,
    error_message,
    analyzed_at,
    created_at,
    updated_at
)
SELECT
    id,
    feedback_id,
    sentiment,
    sentiment_score,
    category,
    category_score,
    matched_keywords,
    priority,
    priority_score,
    priority_reason,
    'seed-data',
    '1.0',
    'SUCCESS',
    NULL,
    analyzed_at,
    created_at,
    updated_at
FROM analysis_rows;

COMMIT;
