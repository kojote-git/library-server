SELECT
  book.id, DATE(downloads.dateDownloaded) AS dateDownloaded,
  SUM(downloads.dCount) AS totalDownloads
FROM Book book
  LEFT JOIN (
    SELECT
      bookInstance.bookId AS bookId,
      downloads.dateDownloaded AS dateDownloaded,
      SUM(downloads.dCount) AS dCount
    FROM BookInstance bookInstance
    LEFT JOIN (
      SELECT
        bookInstanceId,
        dateDownloaded,
        COUNT(*) AS dCount
      FROM Download
    GROUP BY bookInstanceId, dateDownloaded
    ) AS downloads
    ON bookInstance.id = downloads.bookInstanceId
  GROUP BY bookInstance.bookId, downloads.dateDownloaded
  ) AS downloads
ON downloads.bookId = book.id
WHERE book.id = ?
GROUP BY book.id, DATE(downloads.dateDownloaded)
ORDER BY totalDownloads DESC;