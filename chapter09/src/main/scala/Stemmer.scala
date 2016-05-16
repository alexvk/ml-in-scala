package org.akozlov.examples

import org.apache.spark._

class Stemmer extends Serializable
{
  // Word to be stemmed.
  var b = ""
  // Just recode the existing stuff, then go through and refactor with some intelligence.
  def cons(i: Int): Boolean = {
    b(i) match {
      case 'a' | 'e' | 'i' | 'o' | 'u' => false
      case 'y' => i == 0 || !cons(i-1)
      case _ => true;
    }
  }
  // Add via letter or entire word
  def add(ch: Char) =  { b += ch }
  def add(word: String) =  { b = word }
  /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,
         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         ....
   */
  def calcM(s: String): Int = (0 until b.length).foldLeft((0, false)) { case(r, s) => if (!cons(s)) (r._1, false) else if (!r._2 && s>0) (r._1 + 1, true) else (r._1, true) } ._1
  // Removing the suffix 's', does a vowel exist?'
  def vowelInStem(s: String): Boolean = { !(0 until b.length - s.length).forall(cons(_)) }
  // doublec(j) is true <=> j,(j-1) contain a double consonant.
  def doublec(): Boolean = b.length > 1 && b.takeRight(2)(0) == b.takeRight(2)(1) && cons(b.length - 1)
  def cvc(s: String): Boolean = {
    var i = b.length - 1 - s.length
    return !(i < 2 || !cons(i) || cons(i-1) || !cons(i-2) || b(i) == 'w' || b(i) == 'x' || b(i) == 'y')
  }
  // Returns true if it did the change.
  def replacer(orig: String, replace: String, checker: Int => Boolean ): Boolean =
  {
    var l = b.length
    var origLength = orig.length
    var res = false
    if (b.endsWith(orig))
    {
      var n = b.substring(0, l - origLength)
      var m = calcM(n)
      if (checker(m))
      {
        b = n + replace
      }
      res = true
    }
    return res
  }
  // process the list of tuples to find which prefix matches the case.
  // checker is the conditional checker for m.
  def processSubList(l:List[(String, String)], checker: Int=>Boolean ): Boolean =
  {
    var iter = l.iterator
    var done = false
    while (!done && iter.hasNext )
    {
      var v = iter.next
      done = replacer(v._1, v._2, checker )
    }
    return done
  }
  def step1()
  {
    var l = b.length
    var m = calcM(b)
    // step 1a
    var vals = List(("sses", "ss"), ("ies","i"), ("ss","ss"), ("s", ""))
    processSubList(vals, _>=0)
    // step 1b
    if (!(replacer("eed", "ee", _>0) ) )
    {
      if ((vowelInStem("ed") && replacer("ed", "", _>=0) ) || (vowelInStem("ing") && replacer("ing", "", _>=0)))
      {
        vals = List(("at", "ate"), ("bl","ble"), ("iz","ize"))
        if (! processSubList(vals, _>=0 ) )
        {
          // if this isn't done, then it gets more confusing.
          m = calcM(b)
          var last = b(b.length - 1)
          if (doublec() && !(last == 'l' || last == 's' || last == 'z'))
          {
            b = b.substring(0, b.length - 1)
          }
          else
          if (m == 1 && cvc(""))
          {
            b = b + "e"
          }
        }
      }
    }
    // step 1c
    (vowelInStem("y") && replacer("y", "i", _>=0))
   }
   def step2() =
   {
      var vals = List(("ational", "ate"),("tional","tion"),("enci","ence"),("anci","ance"),("izer","ize"),("bli","ble"),("alli", "al"),
                       ("entli","ent"),("eli","e"),("ousli","ous"),("ization","ize"),("ation","ate"),("ator","ate"),("alism","al"),
                       ("iveness","ive"),("fulness","ful"),("ousness", "ous"),("aliti", "al"),("iviti","ive"),("biliti", "ble"),("logi", "log"))
      processSubList(vals, _>0)
   }
  def step3() =
  {
      var vals = List(("icate", "ic"),("ative",""),("alize","al"),("iciti","ic"),("ical","ic"),("ful",""),("ness",""))
      processSubList(vals, _>0)
  }
  def step4() =
  {
      // first part.
      var vals = List(("al",""),("ance",""),("ence",""),("er",""),("ic",""),("able",""),("ible",""),("ant",""),("ement",""),
                       ("ment",""),("ent",""))
      var res = processSubList(vals, _>1 )
      // special part.
      if (!res)
      {
        if (b.length > 4)
        {
          if (b(b.length - 4 ) == 's' || b(b.length - 4) == 't')
          {
            res = replacer("ion", "", _>1)
          }
        }
      }
      // third part.
      if (!res)
      {
        var vals = List(("ou",""),("ism",""),("ate",""),("iti",""),("ous",""),("ive",""),("ize",""))
        res = processSubList(vals, _>1)
      }
  }
  def step5a() =
  {
      var res = false
      res = replacer("e", "", _>1)
      if (!cvc("e"))
      {
        res = replacer("e", "", _==1)
      }
  }
  def step5b() =
  {
    var res = false
    var m = calcM(b)
    if (m > 1 && doublec() && b.endsWith("l"))
    {
      b = b.substring(0, b.length - 1)
    }
  }
  def stem(s: String): String =
  {
    add(s)
    if (b.length > 2)
    {
      step1()
      step2()
      step3()
      step4()
      step5a()
      step5b()
    }
    b
  }
}

object Stemmer {

  /*
   * Object (singleton): The main program will run stemmer
   *
   * run as:
   * <code>sbt "run-main org.akozlov.examples.Stemmer local[2] shakespeare leotolstoy chekhov bible"</code>
   * or from spark-shell:
   * <code>Stemmer.main(Array("local[2]", "shakespeare", "leotolstoy", "checkov", "nytimes", "bible"))</code>
   *
   */

  def main(args: Array[String]) {

    val conf = new SparkConf().
      setAppName("StemmerExample").
      setMaster(args(0))

    val sc = new SparkContext(conf)

    val stemmer = new Stemmer;

    val stopwords = scala.collection.immutable.TreeSet(
      "", "i", "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "from", "had", "has", "he", "her", "him", "his", "in", "is", "it", "its", "my", "not", "of", "on", "she", "that", "the", "to", "was", "were", "will", "with", "you"
    ) map { stemmer.stem(_) }

    val bags = for (name <- args.slice(1, args.length)) yield {
      val rdd = sc.textFile(name)
      val withcounts = rdd.flatMap(_.split("\\W+")).map(stemmer.stem(_)).filter(!stopwords.contains(_)).map((_, 1)).reduceByKey(_+_)
      val mincount = scala.math.max(1L, 0.0001 * withcounts.count.toLong)
      withcounts.filter(_._2 > mincount ).map(_._1).cache
    }

    for(l <- 0 until { args.length - 1 }; r <- l until { args.length - 1 })
      println("The intersect " + args(l+1) + " x " + args(r+1) + " is: " + bags(l).intersection(bags(r)).count)

    sc.stop
  }
}
