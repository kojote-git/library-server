SELECT
  subject.subject, AVG(rating.rating) AS averageRating
FROM Rating rating
INNER JOIN Book b on rating.bookId = b.id
INNER JOIN WorkSubject s on b.workId = s.workId
INNER JOIN Subject subject on s.subjectId = subject.id
WHERE rating.readerId = ?
GROUP BY subject.subject