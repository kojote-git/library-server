SELECT
  SUM(downloads.downloadCount) AS totalDownloads
FROM Subject subject
INNER JOIN (
  SELECT
    subject.id AS subjectId, downloads.dateDownloaded,
    SUM(downloads.downloadCount) AS downloadCount
  FROM Subject subject
  INNER JOIN WorkSubject workSubject ON subject.id = workSubject.subjectId
  INNER JOIN (
    SELECT
      book.workId, downloads.dateDownloaded,
      SUM(downloads.downloadCount) AS downloadCount
    FROM Book book
    INNER JOIN (
      SELECT
        instance.bookId, downloads.dateDownloaded,
        SUM(downloads.downloadsCount) AS downloadCount
      FROM BookInstance instance
      INNER JOIN (
        SELECT
          bookInstanceId, DATE(dateDownloaded) AS dateDownloaded,
          COUNT(*) AS downloadsCount
        FROM Download
        GROUP BY bookInstanceId, DATE(dateDownloaded)
      ) AS downloads
      ON instance.id = downloads.bookInstanceId
      GROUP BY instance.bookId, downloads.dateDownloaded
    ) AS downloads
    ON downloads.bookId = book.id
    GROUP BY book.workId, downloads.dateDownloaded, downloads.downloadCount
  ) AS downloads
  ON downloads.workId = workSubject.workId
  GROUP BY subject.id, downloads.dateDownloaded
) AS downloads
ON subject.id = downloads.subjectId
WHERE subject.subject = ?
AND downloads.dateDownloaded BETWEEN ? AND ?;

