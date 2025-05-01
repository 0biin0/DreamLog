import '../css/mainpage.css';

function MainPage() {
  return (
    <div className="main-container">
      <div className="content-wrapper">
        <div className="main-heading">
          <h1>당신의 꿈을 기록하세요.</h1>
          <p>하루의 작은 생각이 내일의 현실이 됩니다.</p>
        </div>

        <div className="top-section">
          <div className="block image-block">
            <img src="/img/diary.png" alt="Manifestation Diary" />
          </div>
          <div className="block text-block">
            <div className="block-text">
              생각은 현실이 된다,<br />
              기록에서 시작하세요.
            </div>
          </div>
        </div>

        <div className="bottom-section">
          <div className="block text-block">
            <div className="block-text">
              당신의 내일을, 오늘 여기 적어보세요.📝
            </div>
          </div>
          <div className="block image-block">
            <img src="/img/write.png" alt="Checklist" />
          </div>
        </div>

        <div className="main-footer">
          <p>📘 오늘 하루도, 기록과 함께 천천히.</p>
        </div>
      </div>
    </div>
  );
}

export default MainPage;
