SELECT
  author.id, DATE(workDownloads.dateDownloaded) AS dateDownloaded,
  SUM(workDownloads.dCount) AS downloads
FROM Author author
LEFT JOIN WorkAuthor wa ON author.id = wa.authorId
LEFT JOIN (
  SELECT
    book.workId, SUM(downloads.dCount) AS dCount,
    downloads.dateDownloaded AS dateDownloaded
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
  GROUP BY book.workId, downloads.dateDownloaded
) AS workDownloads
ON wa.workId = workDownloads.workId
WHERE author.id = ?
GROUP BY author.id, DATE(workDownloads.dateDownloaded)
ORDER BY downloads DESC;