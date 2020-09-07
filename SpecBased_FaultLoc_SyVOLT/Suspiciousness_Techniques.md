## Suspiciousness techniques considered in the study <h1>

There are several techniques proposed in the literature for suspiciousness computation. In this work we study how effective they are in the context of model transformation verification, i.e., how helpful they are in finding the faulty transformation rule. For this, we have surveyed papers that apply SBFL in different contexts and have selected the same 18 techniques that were collected in [1]. Each technique is displayed with the corresponding suspiciousness computation formulae in the table displayed below.

As described in [1], *Tarantula* [13] is one of the best-known fault localization techniques. It follows the intuition that statements that are executed primarily by more failed test cases are highly likely to be faulty. Additionally, statements that are executed primarily by more successful test cases are less likely to be faulty. The *Ochiai* similarity coefficient is known from the biology domain and it has been proved to outperform several other coefficients used in fault localization and data clustering [8]. This can be attributed to the Ochiai coefficient being more sensitive to activity of potential fault locations in failed runs than to activity in passed runs.
*Ochiai2* is an extension of this technique [5, 6, 9]. *Kulczynski2*, taken from the field of artificial intelligence, and *Cohen* have showed promising results in preliminary experiments [2, 6]. *Russel-Rao* has shown different results in previous experiments [2, 12, 15], while *Simple Matching* has been used in clustering [6].
*Reogers & Tanimoto* presented a high similarity with *Simple Matching* when ranking in the study performed in [6]. The framework called *Barinel* [3] combines spectrum-based fault localization and model-based debugging to localize single and multiple bugs in programs. Zoltar [14] is also a tool set for fault localization.
*Arithmetic Mean*, *Phi* (*Geometric Mean*), *Op2* and *Pierce* have been considered in some comparative studies with other metrics [2, 5, 6]. *Mountford* behaves as the second best technique, among 17 of them, for a specific program in a study performed in [4], where *Baroni-Urbani & Buser* is also studied. As for *D\**, its numerator, *(N_{CF})^{\*})*, depends on the value of "\*" selected. This technique was the best technique in the study performed in [7] when "*" was assigned a value of 2. We have followed the same approach, so we have *(N_{CF})^{2})* in the numerator of the formula for the *D\** technique.

Note that the computation of these formulae may result in having zero in the denominator.Different approaches mention how to deal with such cases [16, 17, 18]. Following the guidelines of these works, if a denominator is zero and the numerator is also zero, our computation returns zero. However, if the numerator is not 0, then it returns 1 [16].

The notatoin shown in the formulae is the following: <br>
*Ncf*: number of failed PCs exercising the rule <br>
*Nuf*: number of failed PCs not exercising the rule <br>
*Ncs*: number of successful PCs exercising the rule <br>
*Nus*: number of successful PCs not exercising the rule <br>
*Nc*: number of PCs exercising the rule <br>
*Nu*: number of PCs not exercising the rule <br>
*Ns*: number of successful PCs <br>
*Nf*: number of failed PCs <br>


![techniques](images/TechniquesWebsite.png)

[1] J. Troya, S. Segura, J. A. Parejo, and A. Ruiz-Cortés, *Spectrum-based fault localization in model transformations,* ACM Trans. Softw. Eng. Methodol., vol. 27, no. 3, pp. 13:1–13:50, 2018. <br>
[2] X. Xie, T. Y. Chen, F.-C. Kuo, and B. Xu, *A Theoretical Analysis of the Risk Evaluation Formulas for Spectrum-based Fault Localization,* ACM Trans. Softw. Eng. Methodol., vol. 22, no. 4, pp. 31:1–31:40, 2013 <br>
[3] R. Abreu, P. Zoeteweij, and A. J. C. v. Gemund, *Spectrum-Based Multiple Fault Localization,* in Proc. of ASE. IEEE Computer Society, 2009, pp. 88–99. <br>
[4] W. E. Wong, V. Debroy, Y. Li, and R. Gao, *Software Fault Localization Using DStar (D\*),* in Proc. of SERE, 2012, pp. 21–30. <br>
[5] W. E.Wong, R. Gao, Y. Li, R. Abreu, and F.Wotawa, *A Survey on Software Fault Localization,* IEEE Transactions on Software Engineering, vol. 42, no. 8, pp. 707–740, 2016. <br>
[6] L. Naish, H. J. Lee, and K. Ramamohanarao, *A Model for Spectra-based Software Diagnosis,* ACM Trans. Softw. Eng. Methodol., vol. 20, no. 3, pp. 11:1–11:32, 2011. <br>
[7] W. E. Wong, V. Debroy, R. Gao, and Y. Li, *The DStar Method for Effective Software Fault Localization,* IEEE Transactions on Reliability, vol. 63, no. 1, pp. 290–308, 2014. <br>
[8] R. Abreu, P. Zoeteweij, R. Golsteijn, and A. J. van Gemund, *A practical evaluation of spectrum-based fault localization,* Journal of Systems and Software, vol. 82, no. 11, pp. 1780 – 1792, 2009. <br>
[9] F. Y. Assiri and J. M. Bieman, *Fault localization for automated program repair: effectiveness, performance, repair correctness,* Software Quality Journal, vol. 25, no. 1, pp. 171–199, 2017. <br>
[10] A. E. Maxwell and A. E. G. Pilliner, *Deriving coefficients of reliability and agreement for ratings,* British Journal of Mathematical and Statistical Psychology, vol. 21, no. 1, pp. 105–116, 1968. <br>
[11] X. Mao, Y. Lei, Z. Dai, Y. Qi, and C. Wang, *Slice-based Statistical Fault Localization,* J. Syst. Softw., vol. 89, pp. 51–62, 2014. <br>
[12] Y. Qi, X. Mao, Y. Lei, and C. Wang, *Using Automated Program Repair for Evaluating the Effectiveness of Fault Localization Techniques,* in Proc. of ISSTA. ACM, 2013, pp. 191–201. <br>
[13] J. A. Jones and M. J. Harrold, *Empirical Evaluation of the Tarantula Automatic Fault-localization Technique,* in Proc. of ASE. ACM, 2005, pp. 273–282. <br>
[14] T. Janssen, R. Abreu, and A. J. van Gemund, *Zoltar: a spectrumbased fault localization tool,* in Proc. of SINTER@ESEC/FSE. ACM, 2009, pp. 23–30. <br>
[15] X. Xie, *On the Analysis of Spectrum-based Fault Localization,* Ph.D. dissertation, Faculty of Information and Communication Technologies, Swinburne University of Technology, Australia, 2012. <br>
[16] S. Yoo, *Evolving human competitive spectra-based fault localisation techniques,* in Proc. of SSBSE. Berlin, Heidelberg: Springer, 2012, pp. 244–258. <br>
[17] X. Xue and A. S. Namin, *How significant is the effect of fault interactions on coverage-based fault localizations?* in Proc. of ESEM, 2013, pp. 113–122. <br>
[18] L. Naish, Neelofar, and K. Ramamohanarao, *Multiple bug spectral fault localization using genetic programming,* in Proc. of ASWEC, 2015, pp. 11–17.
